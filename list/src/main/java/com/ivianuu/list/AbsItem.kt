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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Single item in a [ItemAdapter]
 */
abstract class AbsItem<H : Holder>(
    id: Any? = null,
    layoutRes: Int = -1,
    properties: Iterable<Any?>? = null
) {

    /**
     * The unique id of this item
     */
    open val id: Long = itemIdFor(id)

    /**
     * The view type of this item
     */
    open val viewType: Int get() = layoutRes

    /**
     * The layout res of this item which will be in [createView] if not overridden
     */
    open val layoutRes = layoutRes

    /**
     * All properties of this item which will be used to produce a correct [equals] and [hashCode]
     */
    open val properties: Iterable<Any?>? = properties

    /**
     * Returns a new holder
     */
    protected abstract fun createHolder(): H

    /**
     * Will be called when a view for item should be created
     */
    protected open fun createView(parent: ViewGroup): View {
        check(layoutRes != -1) { "specify a layoutRes or override createView" }
        return LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
    }

    /**
     * Should bind the data of this item to the [holder]
     */
    protected open fun bind(holder: H) {
    }

    /**
     * Should reverse everything done in [bind]
     */
    protected open fun unbind(holder: H) {
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Item<*>) return false

        if (id != other.id) return false
        if (viewType != other.viewType) return false
        if (properties != other.properties) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + viewType
        result = 31 * result + properties.hashCode()
        return result
    }

    override fun toString(): String {
        return "${javaClass.simpleName}(" +
                "id=$id," +
                "viewType=$viewType," +
                "properties=$properties" +
                ")"
    }

}

/**
 * Adds this item to the [controller]
 */
fun <T : Item<*>> T.addTo(controller: ItemController): T {
    controller.add(this)
    return this
}