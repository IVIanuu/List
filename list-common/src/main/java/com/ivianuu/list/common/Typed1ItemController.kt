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

import com.ivianuu.list.ItemController

/**
 * Typed [ItemController]
 */
abstract class Typed1ItemController<A> : ItemController() {

    var data1: A? = null
        set(value) {
            field = value
            allowItemBuildRequests = true
            requestItemBuild()
            allowItemBuildRequests = false
        }

    private var allowItemBuildRequests = false

    override fun requestItemBuild() {
        if (!allowItemBuildRequests) {
            throw IllegalStateException("cannot be called directly use setData() instead")
        }

        super.requestItemBuild()
    }

    final override fun buildItems() {
        if (!isBuildingItems) {
            throw IllegalStateException("cannot be called directly use setData() instead")
        }
        buildItems(data1!!)
    }

    /**
     * Builds a list of items for the [data1]
     */
    protected abstract fun buildItems(data1: A)

}

/**
 * Simple [Typed1ItemController] which uses the [buildItems] block
 */
class SimpleTyped1ItemController<A>(
    private val buildItems: Typed1ItemController<A>.(A) -> Unit
) : Typed1ItemController<A>() {
    override fun buildItems(data1: A) {
        buildItems.invoke(this, data1)
    }
}

/**
 * Returns a [SimpleTyped1ItemController] which uses [buildItems] to build it's items
 */
fun <A> typed1ItemController(buildItems: Typed1ItemController<A>.(data: A) -> Unit): SimpleTyped1ItemController<A> =
    SimpleTyped1ItemController(buildItems = buildItems)