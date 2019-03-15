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

package com.ivianuu.list.common

import android.view.View
import com.ivianuu.list.ListModel
import com.ivianuu.list.ModelHolder
import kotlinx.android.extensions.LayoutContainer

/**
 * A [ModelHolder] which is also a [LayoutContainer]
 */
open class LayoutContainerHolder : ModelHolder(), LayoutContainer {
    override val containerView: View
        get() = view
}

/**
 * A [ListModel] which uses [LayoutContainerHolder]s
 */
abstract class LayoutContainerModel : ListModel<LayoutContainerHolder>() {
    override fun createHolder(): LayoutContainerHolder = LayoutContainerHolder()
}