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
 * @author Manuel Wrage (IVIanuu)
 */
abstract class ListModel<H : ModelHolder> {

    var id: Long = -1
        internal set(value) {
            check(!addedToController) { "cannot change the id of an added model" }
            field = value
        }

    open val viewType: Int get() = layoutRes

    open val layoutRes = 0

    val properties = ModelProperties()

    private val listeners = mutableSetOf<ListModelListener>()
    private var superCalled = false

    private lateinit var controller: ModelController
    private var addedToController = false
    private var blockMutations = false

    protected abstract fun onCreateHolder(): H

    protected open fun onBuildView(parent: ViewGroup): View {
        check(layoutRes != 0) { "specify a layoutRes if you don't override onBuildView" }
        return LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
    }

    protected open fun onBind(holder: H) {
        superCalled = true
    }

    protected open fun onUnbind(holder: H) {
        superCalled = true
    }

    protected open fun onAttach(holder: H) {
        superCalled = true
    }

    protected open fun onDetach(holder: H) {
        superCalled = true
    }

    protected open fun onFailedToRecycle(holder: H): Boolean = true

    protected fun <T> property(
        key: String,
        doHash: Boolean = true,
        defaultValue: () -> T
    ): ModelPropertyDelegate<T> = ModelPropertyDelegate(this, key, doHash, defaultValue)

    protected fun <T> requiredProperty(
        key: String,
        doHash: Boolean = true
    ): ModelPropertyDelegate<T> = ModelPropertyDelegate(this, key, doHash) {
        error("missing property with key $key use optionalProperty() for optional ones")
    }

    protected fun <T> optionalProperty(
        key: String,
        doHash: Boolean = true
    ): ModelPropertyDelegate<T?> =
        ModelPropertyDelegate(this, key, doHash) { null }

    fun addListener(listener: ListModelListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: ListModelListener) {
        listeners.remove(listener)
    }

    internal fun createHolder(): H {
        notifyListeners { it.preCreateHolder(this) }
        val holder = onCreateHolder()
        notifyListeners { it.postCreateHolder(this, holder) }
        return holder
    }

    internal fun buildView(parent: ViewGroup): View {
        notifyListeners { it.preCreateView(this) }
        val view = onBuildView(parent)
        notifyListeners { it.postCreateView(this, view) }
        return view
    }

    internal fun bind(holder: H) {
        notifyListeners { it.preBind(this, holder) }
        requireSuperCalled { onBind(holder) }
        notifyListeners { it.postBind(this, holder) }
    }

    internal fun unbind(holder: H) {
        notifyListeners { it.preUnbind(this, holder) }
        requireSuperCalled { onUnbind(holder) }
        notifyListeners { it.postUnbind(this, holder) }
    }

    internal fun attach(holder: H) {
        notifyListeners { it.preAttach(this, holder) }
        requireSuperCalled { onAttach(holder) }
        notifyListeners { it.postAttach(this, holder) }
    }

    internal fun detach(holder: H) {
        notifyListeners { it.preDetach(this, holder) }
        requireSuperCalled { onDetach(holder) }
        notifyListeners { it.postDetach(this, holder) }
    }

    internal fun failedToRecycleView(holder: H): Boolean {
        val result = onFailedToRecycle(holder)
        notifyListeners { it.onFailedToRecycleView(this, holder) }
        return result
    }

    internal fun addedToController(controller: ModelController) {
        check(!addedToController) {
            "already added to a controller ${this.controller} cannot add to $controller"
        }
        check(id != 0L) { "id must be set" }

        this.controller = controller
        addedToController = true
    }

    internal fun blockMutations() {
        blockMutations = true
        properties.blockMutations()
    }

    private inline fun notifyListeners(block: (ListModelListener) -> Unit) {
        (controller.modelListeners + listeners.toList()).forEach(block)
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
                "viewType=$viewType" +
                "properties=$properties" +
                ")"
    }
}

fun <T> ListModel<*>.getProperty(key: String): T? =
    properties.getProperty(key)

fun <T> ListModel<*>.requireProperty(key: String): T =
    properties.requireProperty(key)

fun <T> ListModel<*>.setProperty(
    key: String,
    value: T,
    doHash: Boolean = true
) {
    properties.setProperty(key, value, doHash)
}

fun ListModel<*>.id(id: Any?) {
    this.id = id?.hashCode()?.toLong() ?: 0
}

fun <T : ListModel<*>> T.addTo(controller: ModelController): T {
    controller.addInternal(this)
    return this
}