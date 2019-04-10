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
 * Delegate which will be used to read and write [ModelProperties] in [ListModel]s
 */
class ModelPropertyDelegate<T>(
    private val properties: ModelProperties,
    internal val key: String,
    private val doHash: Boolean = true,
    private val onPropertySet: ((T) -> Unit)? = null,
    private val defaultValue: () -> T
) : ReadWriteProperty<ListModel<*>, T> {

    init {
        properties.registerDelegate(this)
    }

    override fun getValue(thisRef: ListModel<*>, property: KProperty<*>): T {
        return getValueInternal()
    }

    override fun setValue(thisRef: ListModel<*>, property: KProperty<*>, value: T) {
        properties.setProperty(key, value, doHash)
        onPropertySet?.invoke(value)
    }

    internal fun initializeValue() {
        getValueInternal()
    }

    private fun getValueInternal(): T {
        var property = properties.getPropertyEntry<T>(key)

        if (property == null) {
            property = ModelProperty(
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