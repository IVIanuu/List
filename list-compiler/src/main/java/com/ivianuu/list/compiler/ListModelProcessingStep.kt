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
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType

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

        val constructorParams = element
            .enclosedElements
            .filterIsInstance<ExecutableElement>()
            .first { it.kind == ElementKind.CONSTRUCTOR }
            .parameters
            .map { param ->
                ConstructorParamDescriptor(
                    param.asType().asTypeName().javaToKotlinType(),
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

                val setterFunction = type.enclosedElements
                    .filterIsInstance<ExecutableElement>()
                    .firstOrNull {
                        it.simpleName.toString().startsWith("set${fieldName.capitalize()}")
                    }

                if (setterFunction == null || setterFunction.modifiers.contains(Modifier.PRIVATE)) {
                    return@mapNotNull null
                }

                val isInternal =
                    setterFunction.simpleName.toString() != "set${fieldName.capitalize()}"

                val fieldType = type.enclosedElements
                    .filterIsInstance<ExecutableElement>()
                    .first { it.simpleName.startsWith("get${fieldName.capitalize()}") }
                    .returnType
                    .asTypeName()
                    .javaToKotlinType()

                ListPropertyDescriptor(fieldType, fieldName, isInternal)
            }

        return ListModelDescriptor(
            element,
            elementClass,
            elementClass.packageName,
            element.simpleName.toString() + "ListExt",
            dslBuilderName,
            !element.modifiers.contains(Modifier.ABSTRACT),
            constructorParams,
            properties
        )
    }
}