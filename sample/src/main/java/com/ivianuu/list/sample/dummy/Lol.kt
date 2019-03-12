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

package com.ivianuu.list.sample.dummy

import com.ivianuu.list.ListModel
import com.ivianuu.list.annotations.Model
import com.ivianuu.list.common.LayoutContainerHolder
import com.ivianuu.list.sample.MainActivity

@Model internal class ConstructorModel(
    private val lol: String,
    private val nullableSomething: Boolean?,
    private val lambda: (String, Int) -> Unit
) : MyMiddleModel() {
    var tja by optionalProperty<MainActivity>("tja")
}

@Model class MySimpleModel : MyMiddleModel() {
    var title by optionalProperty<String>("title")
    private var desc by optionalProperty<String>("desc")
}

@Model open class MyMiddleModel : MyBaseModel() {
    internal var refreshToken by requiredProperty<List<Int>>("refreshToken")
}

@Model
abstract class MyBaseModel : ListModel<LayoutContainerHolder>() {
    var accessToken by requiredProperty<String>("accessToken")
    var onMenuClick by optionalProperty<(Int, String) -> Unit>("onMenuClick")
    override fun onCreateHolder(): LayoutContainerHolder = LayoutContainerHolder()
}