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

const val USE_PROPERTY_NAME = "ItemPropertyDelegate.usePropertyName"

/**
 * Delegate which will be used to read and write [ItemProperties] in [Item]s
 */
class ItemPropertyDelegate<T>(
    private val properties: ItemProperties,
    private val key: String,
    private val doHash: Boolean = true,
    private val onPropertySet: ((T) -> Unit)? = null,
    private val defaultValue: (ItemPropertyDelegate<T>) -> T
) : ReadWriteProperty<Item<*>, T> {

    lateinit var realKey: String

    override fun getValue(thisRef: Item<*>, property: KProperty<*>): T {
        initRealKeyIfNeeded(thisRef, property)

        var prop = properties.getPropertyEntry<T>(realKey)

        if (prop == null) {
            prop = ItemProperty(
                realKey,
                defaultValue(this),
                doHash
            )

            properties.setProperty(prop)
            onPropertySet?.invoke(prop.value)
        }

        return prop.value
    }

    override fun setValue(thisRef: Item<*>, property: KProperty<*>, value: T) {
        initRealKeyIfNeeded(thisRef, property)
        properties.setProperty(realKey, value, doHash)
        onPropertySet?.invoke(value)
    }

    private fun initRealKeyIfNeeded(thisRef: Item<*>, property: KProperty<*>) {
        if (!this::realKey.isInitialized) {
            realKey = if (key == USE_PROPERTY_NAME) {
                "${thisRef::class.java.simpleName}.${property.name}"
            } else {
                key
            }
        }
    }

}