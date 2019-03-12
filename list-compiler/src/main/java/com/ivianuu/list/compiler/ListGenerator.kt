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

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.asTypeName

class ListGenerator(private val descriptor: ListModelDescriptor) {

    fun generate(): FileSpec {
        return FileSpec.builder(descriptor.packageName, descriptor.fileName)
            .addImport("com.ivianuu.list", "addTo")
            .apply {
                if (descriptor.generateBuilder) {
                    addFunction(listControllerDslBuilder())
                }
            }
            .apply {
                descriptor.properties
                    .map(this@ListGenerator::propertySetter)
                    .forEach { addFunction(it) }
            }
            .build()
    }

    private fun listControllerDslBuilder(): FunSpec {
        return FunSpec.builder(descriptor.dslBuilderName)
            .apply {
                descriptor.constructorParams.forEach { param ->
                    addParameter(param.name, param.param)
                }
            }
            .addParameter(
                ParameterSpec.builder(
                    "block",
                    LambdaTypeName.get(
                        descriptor.listModel,
                        returnType = Unit::class.asTypeName()
                    )
                ).build()
            )
            .receiver(CLASS_MODEL_CONTROLLER)
            .returns(descriptor.listModel)
            .addCode(
                CodeBlock.builder()
                    .addStatement(
                        "return %T(" +
                                descriptor.constructorParams
                                    .map(ConstructorParamDescriptor::name)
                                    .joinToString(", ") +
                                ").apply(block).addTo(this)",

                        descriptor.listModel
                    )
                    .build()
            )
            .build()
    }

    private fun propertySetter(property: ListPropertyDescriptor): FunSpec {
        return FunSpec.builder(property.name)
            .receiver(descriptor.listModel)
            .apply {
                if (property.isInternal) {
                    addModifiers(KModifier.INTERNAL)
                }
            }
            .addParameter(
                ParameterSpec.builder(property.name, property.property).build()
            )
            .addCode(
                CodeBlock.builder()
                    .addStatement("this.${property.name} = ${property.name}")
                    .addStatement("return this")
                    .build()
            )
            .returns(descriptor.listModel)
            .build()
    }
}