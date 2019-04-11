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
 * Properties of a [Item]
 */
class ItemProperties internal constructor() {

    /**
     * All properties
     */
    val entries: Map<String, ItemProperty<*>> get() = _entries
    private val _entries = mutableMapOf<String, ItemProperty<*>>()

    private var itemAdded = false

    private val delegates =
        mutableMapOf<String, ItemPropertyDelegate<*>>()

    /**
     * Returns the [ItemProperty] for the [key]
     */
    fun <T> getPropertyEntry(key: String): ItemProperty<T>? {
        return _entries[key] as? ItemProperty<T>
    }

    /**
     * Sets the [property]
     */
    fun <T> setProperty(
        property: ItemProperty<T>
    ) {
        check(!itemAdded) { "cannot change properties on added items" }
        _entries[property.key] = property
    }

    internal fun itemAdded() {
        itemAdded = true
        // force init the value of all delegates to have consistent equals() and hashCode() results
        delegates.values.toList()
            .forEach { it.itemAdded() }
        delegates.clear() // we don't need the delegate reference anymore
    }

    internal fun registerDelegate(key: String, delegate: ItemPropertyDelegate<*>) {
        check(!itemAdded) { "cannot change properties on added items" }
        delegates[key] = delegate
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ItemProperties) return false

        // get all hashable properties and compare them
        val entries = _entries
            .filterValues { it.doHash }
            .map { it.value }
            .map { it.value }
        val otherEntries = other._entries
            .filterValues { it.doHash }
            .map { it.value }
            .map { it.value }
        if (entries != otherEntries) return false

        return true
    }

    override fun hashCode(): Int {
        // filter out non hashable properties
        val entries = _entries
            .filterValues { it.doHash }
            .map { it.value }
            .map { it.value }
        return entries.hashCode()
    }

    override fun toString(): String {
        val entries = _entries
            .map { it.value }
            .associateBy { it.key }
            .mapValues { it.value.value }
        return entries.toString()
    }

}

/**
 * Returns the property value for [key] or null
 */
fun <T> ItemProperties.getProperty(key: String): T? = getPropertyEntry<T>(key)?.value

/**
 * Returns the property value for [key] or throws
 */
fun <T> ItemProperties.requireProperty(key: String): T = getProperty<T>(key)
    ?: error("missing property for key '$key'")

/**
 * Sets the property
 */
fun <T> ItemProperties.setProperty(
    key: String,
    value: T,
    doHash: Boolean = true
) {
    setProperty(ItemProperty(key, value, doHash))
}

/**
 * Entry of [ItemProperties]
 */
data class ItemProperty<T>(
    val key: String,
    val value: T,
    val doHash: Boolean = true
)