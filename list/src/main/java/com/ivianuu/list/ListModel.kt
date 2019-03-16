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
 * Single item in a [ModelAdapter]
 */
abstract class ListModel<H : ModelHolder> {

    /**
     * The unique id of this model
     */
    var id: Long = -1
        internal set(value) {
            check(!addedToAdapter) { "cannot change the id of an added model" }
            field = value
        }

    /**
     * The view type of this model
     */
    open val viewType: Int get() = layoutRes

    /**
     * The layout res of this model which will be in [buildView] if not overriden
     */
    open val layoutRes = 0

    /**
     * All properties of this model which will be used to implement a correct [equals] and [hashCode]
     */
    val properties = ModelProperties()

    private val listeners = mutableSetOf<ListModelListener>()
    private var superCalled = false

    private lateinit var adapter: ModelAdapter
    private var addedToAdapter = false

    protected abstract fun createHolder(): H

    /**
     * Will be called when a view for model should be created
     */
    protected open fun buildView(parent: ViewGroup): View {
        check(layoutRes != 0) { "specify a layoutRes if you don't override buildView" }
        return LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
    }

    /**
     * Should bindHolder the data of this model to the [holder]
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
        defaultValue: () -> T
    ): ModelPropertyDelegate<T> = ModelPropertyDelegate(this, key, doHash, defaultValue)

    /**
     * Registers a non null property
     */
    protected fun <T> requiredProperty(
        key: String,
        doHash: Boolean = true
    ): ModelPropertyDelegate<T> = ModelPropertyDelegate(this, key, doHash) {
        error("missing property with key $key use optionalProperty() for optional ones")
    }

    /**
     * Registers a nullable property
     */
    protected fun <T> optionalProperty(
        key: String,
        doHash: Boolean = true
    ): ModelPropertyDelegate<T?> =
        ModelPropertyDelegate(this, key, doHash) { null }

    /**
     * Adds the [listener]
     */
    fun addListener(listener: ListModelListener) {
        listeners.add(listener)
    }

    /**
     * Removes the previously added [listener]
     */
    fun removeListener(listener: ListModelListener) {
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

    internal fun unbindView(holder: H) {
        notifyListeners { it.preUnbind(this, holder) }
        requireSuperCalled { unbind(holder) }
        notifyListeners { it.postUnbind(this, holder) }
    }

    internal fun addedToAdapter(adapter: ModelAdapter) {
        check(!addedToAdapter) {
            "already added to another adapter ${this.adapter} cannot add to $adapter"
        }
        check(id != 0L) { "id must be set" }

        this.adapter = adapter
        addedToAdapter = true
        properties.modelAdded()
    }

    private inline fun notifyListeners(block: (ListModelListener) -> Unit) {
        (adapter.modelListeners + listeners.toList()).forEach(block)
    }

    private inline fun requireSuperCalled(block: () -> Unit) {
        superCalled = false
        block()
        check(superCalled) { "super not called" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ListModel<*>) return false

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

/** Calls trough [ModelProperties.getProperty] */
fun <T> ListModel<*>.getProperty(key: String): T? =
    properties.getProperty(key)

/** Calls trough [ModelProperties.requireProperty] */
fun <T> ListModel<*>.requireProperty(key: String): T =
    properties.requireProperty(key)

/** Calls trough [ModelProperties.setProperty] */
fun <T> ListModel<*>.setProperty(
    key: String,
    value: T,
    doHash: Boolean = true
) {
    properties.setProperty(key, value, doHash)
}

/**
 * Sets the hashed version of [id] as the model id
 */
fun ListModel<*>.id(id: Any?) {
    this.id = id?.hashCode()?.toLong() ?: 0
}

/**
 * Adds this model to the [controller]
 */
fun <T : ListModel<*>> T.addTo(controller: ModelController): T {
    controller.addInternal(this)
    return this
}