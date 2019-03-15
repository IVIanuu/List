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

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Properties of a [ListModel]
 */
class ModelProperties internal constructor() {

    /**
     * All properties
     */
    val entries: Map<String, ModelProperty<*>> get() = _entries
    private val _entries = mutableMapOf<String, ModelProperty<*>>()

    private var modelAdded = false

    private val uninitializedDelegates =
        mutableMapOf<String, ModelPropertyDelegate<*>>()

    /**
     * Returns the [ModelProperty] for the [key]
     */
    fun <T> getPropertyEntry(key: String): ModelProperty<T>? =
        _entries[key] as? ModelProperty<T>

    /**
     * Sets the [property]
     */
    fun <T> setProperty(
        property: ModelProperty<T>
    ) {
        check(!modelAdded) { "cannot change properties on added models" }
        _entries[property.key] = property
        uninitializedDelegates.remove(property.key)
    }

    internal fun modelAdded() {
        // force init the value of all delegates to have consistent equals() and hashCode() results
        uninitializedDelegates.values.toList()
            .forEach(ModelPropertyDelegate<*>::initializeValue)
        uninitializedDelegates.clear()

        modelAdded = true
    }

    internal fun registerDelegate(delegate: ModelPropertyDelegate<*>) {
        check(!modelAdded) { "cannot change properties on added models" }
        uninitializedDelegates[delegate.key] = delegate
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ModelProperties) return false

        // get all hashable properties and compare them
        val entries = _entries
            .filterValues(ModelProperty<*>::doHash)
            .map(Map.Entry<String, ModelProperty<*>>::value)
            .map(ModelProperty<*>::value)
        val otherEntries = other._entries
            .filterValues(ModelProperty<*>::doHash)
            .map(Map.Entry<String, ModelProperty<*>>::value)
            .map(ModelProperty<*>::value)
        if (entries != otherEntries) return false

        return true
    }

    override fun hashCode(): Int {
        // filter out non hashable properties
        val entries = _entries
            .filterValues(ModelProperty<*>::doHash)
            .map(Map.Entry<String, ModelProperty<*>>::value)
            .map(ModelProperty<*>::value)
        return entries.hashCode()
    }

    override fun toString(): String {
        val entries = _entries
            .map(Map.Entry<String, ModelProperty<*>>::value)
            .associateBy(ModelProperty<*>::key)
            .mapValues { it.value.value }
        return entries.toString()
    }

}

/**
 * Returns the property value for [key] or null
 */
fun <T> ModelProperties.getProperty(key: String): T? = getPropertyEntry<T>(key)?.value

/**
 * Returns the property value for [key] or throws
 */
fun <T> ModelProperties.requireProperty(key: String): T = getProperty<T>(key)
    ?: error("missing property for key $key")

/**
 * Sets the property
 */
fun <T> ModelProperties.setProperty(
    key: String,
    value: T,
    doHash: Boolean = true
) {
    setProperty(ModelProperty(key, value, doHash))
}

/**
 * Entry of [ModelProperties]
 */
data class ModelProperty<T>(
    val key: String,
    val value: T,
    val doHash: Boolean = true
)

/**
 * Delegate which will be used to read and write [ModelProperties] in [ListModel]s
 */
class ModelPropertyDelegate<T>(
    private val model: ListModel<*>,
    internal val key: String,
    private val doHash: Boolean = true,
    private val defaultValue: () -> T
) : ReadWriteProperty<ListModel<*>, T> {

    init {
        model.properties.registerDelegate(this)
    }

    override fun getValue(thisRef: ListModel<*>, property: KProperty<*>): T {
        return getValueInternal()
    }

    override fun setValue(thisRef: ListModel<*>, property: KProperty<*>, value: T) {
        model.properties.setProperty(key, value, doHash)
    }

    internal fun initializeValue() {
        getValueInternal()
    }

    private fun getValueInternal(): T {
        var property = model.properties.getPropertyEntry<T>(key)

        if (property == null) {
            property = ModelProperty(
                key,
                defaultValue(),
                doHash
            )

            model.properties.setProperty(property)
        }

        return property.value
    }
}