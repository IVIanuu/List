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

import com.ivianuu.list.ModelController

/**
 * Typed [ModelController]
 */
abstract class Typed1ModelController<A> : ModelController() {

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

    /**
     * Builds a list of models for the [data1]
     */
    protected abstract fun buildModels(data1: A)

}

/**
 * Simple [Typed1ModelController] which uses the [buildModels] block
 */
class SimpleTyped1ModelController<A>(
    private val buildModels: Typed1ModelController<A>.(A) -> Unit
) : Typed1ModelController<A>() {
    override fun buildModels(data1: A) {
        buildModels.invoke(this, data1)
    }
}

/**
 * Returns a [SimpleTyped1ModelController] which uses [buildModels] to build it's models
 */
fun <A> typed1ModelController(buildModels: Typed1ModelController<A>.(data: A) -> Unit): SimpleTyped1ModelController<A> =
    SimpleTyped1ModelController(buildModels = buildModels)