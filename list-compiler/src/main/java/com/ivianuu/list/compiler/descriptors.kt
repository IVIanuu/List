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

import com.squareup.kotlinpoet.TypeName
import javax.lang.model.element.Element

data class ListModelDescriptor(
    val target: Element,
    val listModel: TypeName,
    val packageName: String,
    val fileName: String,
    val dslBuilderName: String,
    val generateBuilder: Boolean,
    val constructorParams: List<ConstructorParamDescriptor>,
    val properties: List<ListPropertyDescriptor>
)

data class ListPropertyDescriptor(
    val property: TypeName,
    val name: String,
    val isInternal: Boolean
)

data class ConstructorParamDescriptor(
    val param: TypeName,
    val name: String
)