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

import com.google.common.collect.SetMultimap
import com.ivianuu.list.annotations.Model
import com.ivianuu.processingx.filer
import com.ivianuu.processingx.steps.ProcessingStep
import com.squareup.kotlinpoet.asClassName
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.isDelegated
import me.eugeniomarletti.kotlin.metadata.isPrimary
import me.eugeniomarletti.kotlin.metadata.isVal
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.visibility
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.tools.Diagnostic
import kotlin.reflect.KClass

class ListModelProcessingStep : ProcessingStep() {

    override fun annotations() = setOf(Model::class)

    override fun process(elementsByAnnotation: SetMultimap<KClass<out Annotation>, Element>): Set<Element> {
        elementsByAnnotation[Model::class]
            .filterIsInstance<TypeElement>()
            .mapNotNull(this::createDescriptor)
            .map(::ListGenerator)
            .map(ListGenerator::generate)
            .forEach { it.writeTo(filer) }

        return emptySet()
    }

    private fun createDescriptor(element: TypeElement): ListModelDescriptor? {
        val metadata = element.kotlinMetadata as? KotlinClassMetadata

        if (metadata == null) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                "@Model cannot be used in non kotlin classes", element
            )
            return null
        }

        val classProto = metadata.data.classProto
        val nameResolver = metadata.data.nameResolver

        // todo check if element is a list model

        val elementClass = element.asClassName()

        val dslBuilderName = if (elementClass.simpleName.endsWith("Model")) {
            elementClass.simpleName.replace("Model", "")
        } else {
            elementClass.simpleName
        }.decapitalize()

        val isTypeInternal = classProto.visibility == ProtoBuf.Visibility.INTERNAL

        val constructor = classProto.constructorList
            .firstOrNull { it.isPrimary }

        // todo what to do here
        if (constructor == null) {
            return null
        }

        val constructorParams = constructor.valueParameterList
            .mapNotNull { valueParam ->
                val typeName = valueParam.type.asTypeName(
                    nameResolver, classProto::getTypeParameter,
                    true
                )
                ConstructorParamDescriptor(
                    typeName,
                    nameResolver.getString(valueParam.name),
                    // todo this is ugly find a better way
                    typeName.toString().startsWith("kotlin.Function")
                )
            }

        val properties = mutableListOf<ListPropertyDescriptor>()

        for (metadataForType in element.collectAllTypeMetadatas()) {
            val nameResolverForType = metadataForType.data.nameResolver
            val classProtoForType = metadataForType.data.classProto

            val typeName = nameResolverForType.getString(classProtoForType.fqName)

            for (propertyInType in classProtoForType.propertyList) {
                // no private fields
                if (propertyInType.visibility == ProtoBuf.Visibility.PRIVATE) continue

                if (propertyInType.isVal) continue

                // todo find a better way to check if its a ModelPropertyDelegate
                if (propertyInType.isDelegated) {
                    val propertyName = nameResolverForType.getString(propertyInType.name)

                    val typeElement = processingEnv.elementUtils
                        .getTypeElement(typeName.replace("/", "."))

                    val delegateField = typeElement
                        .enclosedElements
                        .filterIsInstance<VariableElement>()
                        .first { it.simpleName.toString() == "$propertyName\$delegate" }

                    if (delegateField.asType().toString() == CLASS_MODEL_PROPERTY_DELEGATE.toString()) {
                        properties.add(
                            ListPropertyDescriptor(
                                propertyInType.returnType.asTypeName(
                                    nameResolverForType, classProto::getTypeParameter,
                                    useAbbreviatedType = true, allowFlexibleTypes = false
                                ),
                                propertyName,
                                classProto.visibility == ProtoBuf.Visibility.INTERNAL
                                        || propertyInType.visibility == ProtoBuf.Visibility.INTERNAL
                            )
                        )
                    }
                }
            }
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
        )
    }

    private fun TypeElement.collectAllTypeMetadatas(): List<KotlinClassMetadata> {
        val allTypes = mutableListOf<KotlinClassMetadata>()

        var currentElement: TypeElement? = this

        while (currentElement != null) {
            val metadata = currentElement.kotlinMetadata as? KotlinClassMetadata ?: break
            allTypes.add(metadata)
            currentElement = metadata.data.classProto.supertypeList
                .mapNotNull {
                    processingEnv.elementUtils.getTypeElement(
                        metadata.data.nameResolver.getString(it.className)
                            .replace("/", ".")
                    )
                }
                .firstOrNull()
        }

        return allTypes
    }
}