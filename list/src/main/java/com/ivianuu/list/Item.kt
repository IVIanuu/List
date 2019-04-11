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
import com.ivianuu.closeable.Closeable

/**
 * Single item in a [ItemAdapter]
 */
abstract class Item<H : Holder>(
    id: Any? = null,
    layoutRes: Int = -1
) {

    /**
     * The unique id of this item
     */
    var id: Long = -1
        internal set(value) {
            check(!addedToAdapter) { "cannot change the id item after being added to an adapter" }
            field = value
        }

    /**
     * The view type of this item
     */
    open val viewType: Int get() = layoutRes

    /**
     * The layout res of this item which will be in [buildView] if not overridden
     */
    open val layoutRes = layoutRes

    /**
     * All properties of this item which will be used to produce a correct [equals] and [hashCode]
     */
    val properties = ItemProperties()

    private val listeners = mutableSetOf<ItemListener>()
    private var superCalled = false

    private lateinit var adapter: ItemAdapter
    private var addedToAdapter = false

    init {
        id(id)
    }

    /**
     * Returns a new holder
     */
    protected abstract fun createHolder(): H

    /**
     * Will be called when a view for item should be created
     */
    protected open fun buildView(parent: ViewGroup): View {
        check(layoutRes != -1) { "specify a layoutRes or override buildView" }
        return LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
    }

    /**
     * Should bind the data of this item to the [holder]
     */
    protected open fun bind(holder: H) {
        superCalled = true
    }

    /**
     * Should reverse everything done in [bind]
     */
    protected open fun unbind(holder: H) {
        superCalled = true
    }

    /**
     * Registers a property
     */
    protected fun <T> property(
        key: String,
        doHash: Boolean = true,
        onPropertySet: ((T) -> Unit)? = null,
        defaultValue: () -> T
    ): ItemPropertyDelegate<T> {
        return ItemPropertyDelegate(properties, key, doHash, onPropertySet, defaultValue)
    }

    /**
     * Registers a required property which is also the id of this item
     */
    protected fun <T> idProperty(
        key: String,
        onPropertySet: ((T) -> Unit)? = null
    ): ItemPropertyDelegate<T> {
        return ItemPropertyDelegate(properties, key, true, {
            id(it)
            onPropertySet?.invoke(it)
        }) {
            error("missing property with key '$key' use optionalProperty() for optional ones")
        }
    }

    /**
     * Registers a non null property
     */
    protected fun <T> requiredProperty(
        key: String,
        doHash: Boolean = true,
        onPropertySet: ((T) -> Unit)? = null
    ): ItemPropertyDelegate<T> {
        return ItemPropertyDelegate(properties, key, doHash, onPropertySet) {
            error("missing property with key '$key' use optionalProperty() for optional ones")
        }
    }

    /**
     * Registers a nullable property
     */
    protected fun <T> optionalProperty(
        key: String,
        doHash: Boolean = true,
        onPropertySet: ((T?) -> Unit)? = null
    ): ItemPropertyDelegate<T?> {
        return ItemPropertyDelegate(properties, key, doHash, onPropertySet) { null }
    }

    /** Calls trough [ItemEvents.setCallback] */
    operator fun <T> ItemEvents<T>.invoke(block: T) {
        setCallback(block)
    }

    /**
     * Adds the [listener]
     */
    fun addListener(listener: ItemListener): Closeable {
        listeners.add(listener)
        return Closeable { removeListener(listener) }
    }

    /**
     * Removes the previously added [listener]
     */
    fun removeListener(listener: ItemListener) {
        listeners.remove(listener)
    }

    internal fun newHolder(): H {
        val holder = createHolder()
        notifyListeners { it.onCreateHolder(this, holder) }
        return holder
    }

    internal fun createView(parent: ViewGroup): View {
        val view = buildView(parent)
        notifyListeners { it.onBuildView(this, view) }
        return view
    }

    internal fun bindHolder(holder: H) {
        notifyListeners { it.preBind(this, holder) }
        requireSuperCalled { bind(holder) }
        notifyListeners { it.postBind(this, holder) }
    }

    internal fun unbindHolder(holder: H) {
        notifyListeners { it.preUnbind(this, holder) }
        requireSuperCalled { unbind(holder) }
        notifyListeners { it.postUnbind(this, holder) }
    }

    internal fun addedToAdapter(adapter: ItemAdapter) {
        if (addedToAdapter && this.adapter == adapter) return

        check(id != 0L) { "id must be set" }

        check(!addedToAdapter) {
            "already added to another adapter ${this.adapter} cannot add to $adapter"
        }

        this.adapter = adapter
        addedToAdapter = true
        properties.itemAdded()
    }

    private inline fun notifyListeners(block: (ItemListener) -> Unit) {
        (adapter.itemListeners + listeners.toList()).forEach(block)
    }

    private inline fun requireSuperCalled(block: () -> Unit) {
        superCalled = false
        block()
        check(superCalled) { "super not called" }
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

/** Calls trough [ItemProperties.getProperty] */
fun <T> Item<*>.getProperty(key: String): T? =
    properties.getProperty(key)

/** Calls trough [ItemProperties.requireProperty] */
fun <T> Item<*>.requireProperty(key: String): T =
    properties.requireProperty(key)

/** Calls trough [ItemProperties.setProperty] */
fun <T> Item<*>.setProperty(
    key: String,
    value: T,
    doHash: Boolean = true
) {
    properties.setProperty(key, value, doHash)
}

/**
 * Sets the hashed version of [id] as the item id
 */
fun Item<*>.id(id: Any?) {
    this.id = id?.hashCode()?.toLong() ?: 0
}

/**
 * Adds this item to the [controller]
 */
fun <T : Item<*>> T.addTo(controller: ItemController): T {
    controller.add(this)
    return this
}