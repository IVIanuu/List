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

private object UNINITIALIZED

/**
 * Delegate which will be used to read and write [ItemProperties] in [Item]s
 */
class ItemPropertyDelegate<T>(
    private val properties: ItemProperties,
    internal val key: String,
    private val doHash: Boolean = true,
    private val onPropertySet: ((T) -> Unit)? = null,
    private val defaultValue: () -> T
) : ReadWriteProperty<Item<*>, T> {

    private var finalValue: Any? = UNINITIALIZED

    init {
        properties.registerDelegate(key, this)
    }

    override fun getValue(thisRef: Item<*>, property: KProperty<*>): T {
        return getValueInternal()
    }

    override fun setValue(thisRef: Item<*>, property: KProperty<*>, value: T) {
        properties.setProperty(key, value, doHash)
        onPropertySet?.invoke(value)
    }

    internal fun itemAdded() {
        // lock down the value because it cannot change anymore at this point
        // this leads to faster reads because we don't need to query the property
        finalValue = getOrInitializePropertyValue()
    }

    private fun getValueInternal(): T {
        // if we already got the final value return it otherwise read from the properties
        return if (finalValue !== UNINITIALIZED) {
            finalValue as T
        } else {
            getOrInitializePropertyValue()
        }
    }

    private fun getOrInitializePropertyValue(): T {
        var property = properties.getPropertyEntry<T>(key)

        // create the property if it doesn't exist yet
        if (property == null) {
            property = ItemProperty(
                key,
                defaultValue(),
                doHash
            )

            properties.setProperty(property)
            onPropertySet?.invoke(property.value)
        }

        return property.value
    }
}