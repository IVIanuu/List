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

import com.ivianuu.list.ListPlugins
import com.ivianuu.list.ModelController
import com.ivianuu.list.defaultBuildingExecutor
import com.ivianuu.list.defaultDiffingExecutor
import java.util.concurrent.Executor

/**
 * @author Manuel Wrage (IVIanuu)
 */
abstract class Typed1ModelController<A>(
    diffingExecutor: Executor = ListPlugins.defaultDiffingExecutor,
    buildingExecutor: Executor = ListPlugins.defaultBuildingExecutor
) : ModelController(diffingExecutor, buildingExecutor) {

    var data1: A? = null
        set(value) {
            field = value
            allowModelBuildRequests = true
            requestModelBuild()
            allowModelBuildRequests = false
        }

    private var allowModelBuildRequests = false

    override fun requestModelBuild() {
        if (!allowModelBuildRequests) {
            throw IllegalStateException("cannot be called directly use setData() instead")
        }

        super.requestModelBuild()
    }

    final override fun buildModels() {
        if (!isBuildingModels) {
            throw IllegalStateException("cannot be called directly use setData() instead")
        }
        buildModels(data1!!)
    }

    protected abstract fun buildModels(data1: A)

}

class SimpleTyped1ModelController<A>(
    private val buildModels: (A) -> Unit
) : Typed1ModelController<A>() {
    override fun buildModels(data1: A) {
        buildModels.invoke(data1)
    }
}

fun <A> typed1ModelController(buildModels: (A) -> Unit): SimpleTyped1ModelController<A> =
    SimpleTyped1ModelController(buildModels = buildModels)