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

package com.ivianuu.list

/**
 * Controller of a underlying [ItemAdapter]
 */
abstract class ItemController {

    /**
     * The adapter baked by this controller
     */
    open val adapter = ItemAdapter()

    /**
     * Whether or not items are currently building
     */
    @Volatile var isBuildingItems = false
        private set

    private val currentItems = mutableListOf<Item<*>>()

    @Volatile private var hasBuiltItemsEver = false

    private val buildItemsAction: () -> Unit = {
        cancelPendingItemBuild()

        isBuildingItems = true
        buildItems()
        isBuildingItems = false

        adapter.setItems(currentItems.toList())
        currentItems.clear()

        hasBuiltItemsEver = true
    }

    @Volatile private var requestedItemBuildType = RequestedItemBuildType.NONE

    /**
     * Requests a call to [buildItems]
     */
    open fun requestItemBuild() {
        if (hasBuiltItemsEver) {
            requestDelayedItemBuild(0)
        } else {
            buildItemsAction()
        }
    }

    /**
     * Enqueues a delayed call to [buildItems]
     */
    open fun requestDelayedItemBuild(delayMs: Long): Unit = synchronized(this) {
        if (requestedItemBuildType == RequestedItemBuildType.DELAYED) {
            cancelPendingItemBuild()
        } else if (requestedItemBuildType == RequestedItemBuildType.NEXT_FRAME) {
            return@requestDelayedItemBuild
        }

        requestedItemBuildType =
            if (delayMs == 0L) RequestedItemBuildType.NEXT_FRAME else RequestedItemBuildType.DELAYED

        backgroundThread(delayMs, buildItemsAction)
    }

    /**
     * Cancels all pending calls to [buildItems]
     */
    fun cancelPendingItemBuild(): Unit = synchronized(this) {
        if (requestedItemBuildType != RequestedItemBuildType.NONE) {
            requestedItemBuildType = RequestedItemBuildType.NONE
            cancelBackgroundThread(buildItemsAction)
        }
    }

    /**
     * Builds the list of items and adds them via [add]
     */
    protected abstract fun buildItems()

    /**
     * Adds all [items]
     */
    fun add(items: Iterable<Item<*>>) {
        items.forEach { add(it) }
    }

    /**
     * Adds all [items]
     */
    fun add(vararg items: Item<*>) {
        items.forEach { add(it) }
    }

    /**
     * Adds the item
     */
    fun add(item: Item<*>) {
        check(isBuildingItems) { "cannot add items outside of buildItems()" }
        currentItems.add(item)
    }

    inline fun <T : Item<*>> T.add(block: T.() -> Unit): T =
        apply(block).addTo(this@ItemController)

    inline fun <T : Item<*>> T.addIt(block: (T) -> Unit): T =
        apply(block).addTo(this@ItemController)

    private enum class RequestedItemBuildType {
        NONE, NEXT_FRAME, DELAYED
    }

}