/*
 * Copyright 2018 Manuel Wrage
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivianuu.list.compiler

import com.google.auto.common.MoreTypes
import com.google.common.collect.SetMultimap
import com.ivianuu.list.annotations.Model
import com.ivianuu.processingx.ProcessingStep
import com.ivianuu.processingx.write
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.isVal
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.metadata.setterVisibility
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.visibility
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType
import javax.tools.Diagnostic

class ListModelProcessingStep(
    private val processingEnv: ProcessingEnvironment
) : ProcessingStep {

    override fun annotations() = setOf(Model::class.java)

    override fun process(elementsByAnnotation: SetMultimap<Class<out Annotation>, Element>): Set<Element> {
        elementsByAnnotation[Model::class.java]
            .filterIsInstance<TypeElement>()
            .mapNotNull(this::createDescriptor)
            .map(::ListGenerator)
            .map(ListGenerator::generate)
            .forEach { it.write(processingEnv) }

        return emptySet()
    }

    private fun createDescriptor(element: TypeElement): ListModelDescriptor? {
        // todo check if the element is a kotlin class and a list model

        val elementClass = element.asClassName()

        val dslBuilderName = if (elementClass.simpleName.endsWith("Model")) {
            elementClass.simpleName.replace("Model", "")
        } else {
            elementClass.simpleName
        }.decapitalize()

        val isTypeInternal = (element.kotlinMetadata as KotlinClassMetadata)
            .data.classProto.visibility == ProtoBuf.Visibility.INTERNAL

        val constructorParams = element
            .enclosedElements
            .filterIsInstance<ExecutableElement>()
            .first { it.kind == ElementKind.CONSTRUCTOR }
            .parameters
            .mapNotNull { param ->
                val kotlinMetaData = (element.kotlinMetadata as KotlinClassMetadata)
                val property = kotlinMetaData.data.classProto
                    .constructorList
                    .flatMap { it.valueParameterList }
                    .firstOrNull {
                        kotlinMetaData.data.nameResolver.getString(it.name) ==
                                param.simpleName.toString()
                    } ?: return@mapNotNull null

                val type = param.asType().asTypeName().javaToKotlinType()
                    .let { it.copy(nullable = property.type.nullable) }

                ConstructorParamDescriptor(
                    type,
                    param.simpleName.toString()
                )
            }

        val superClasses = mutableListOf<TypeElement>()
        var superClass = MoreTypes.nonObjectSuperclass(
            processingEnv.typeUtils, processingEnv.elementUtils, element.asType() as DeclaredType
        ).orNull()

        while (superClass != null) {
            superClasses.add(superClass.asElement() as TypeElement)
            superClass = MoreTypes.nonObjectSuperclass(
                processingEnv.typeUtils, processingEnv.elementUtils, superClass
            ).orNull()
        }

        val properties = (listOf(element) + superClasses)
            .flatMap { type ->
                type.enclosedElements
                    .filterIsInstance<VariableElement>()
                    .map { type to it }
            }
            .filter { (_, field) ->
                field.asType().asTypeName() == CLASS_MODEL_PROPERTY_DELEGATE
                        && field.simpleName.toString().endsWith("\$delegate")
            }
            .mapNotNull { (type, field) ->
                val fieldName = field.simpleName.toString()
                    .replace("\$delegate", "")

                val kotlinMetaData = (type.kotlinMetadata as KotlinClassMetadata)
                val property = kotlinMetaData.data.classProto
                    .propertyList.firstOrNull {
                    kotlinMetaData.data.nameResolver.getString(it.name) == fieldName
                } ?: return@mapNotNull null

                // ignore private setters
                if (property.visibility == ProtoBuf.Visibility.PRIVATE) return@mapNotNull null

                // ignore private setters
                if (property.setterVisibility == ProtoBuf.Visibility.PRIVATE) return@mapNotNull null

                // ignore val
                if (property.isVal) return@mapNotNull null

                val isInternal =
                    isTypeInternal || property.setterVisibility == ProtoBuf.Visibility.INTERNAL

                val isNullable =
                    property.returnType.nullable

                processingEnv.messager.printMessage(
                    Diagnostic.Kind.WARNING,
                    "property class name -> ${kotlinMetaData.data.nameResolver.getString(property.returnType.className)}"
                )

                val fieldType = type.enclosedElements
                    .filterIsInstance<ExecutableElement>()
                    .first { it.simpleName.startsWith("get${fieldName.capitalize()}") }
                    .returnType
                    .asTypeName()
                    .javaToKotlinType()
                    .let { it.copy(nullable = isNullable) }

                ListPropertyDescriptor(fieldType, fieldName, isInternal)
            }

        return ListModelDescriptor(
            element,
            elementClass,
            elementClass.packageName,
            element.simpleName.toString() + "ListExt",
            dslBuilderName,
            !element.modifiers.contains(Modifier.ABSTRACT),
            isTypeInternal,
            constructorParams,
            properties
        ).also {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.WARNING,
                "descriptor -> $it"
            )
        }
    }
}