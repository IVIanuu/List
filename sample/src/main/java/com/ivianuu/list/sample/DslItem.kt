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

package com.ivianuu.list.sample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ivianuu.list.Item
import com.ivianuu.list.ItemController
import com.ivianuu.list.addTo
import com.ivianuu.list.common.KotlinHolder
import com.ivianuu.list.common.KotlinItem
import kotlin.properties.Delegates

class DslItemBuilder internal constructor() {

    private var id: Any by Delegates.notNull()

    private var bind: ((KotlinHolder) -> Unit)? = null
    private var unbind: ((KotlinHolder) -> Unit)? = null

    private var createView: (ViewGroup) -> View by Delegates.notNull()

    private var viewType: Int? = null
    private var viewTypeSet = false

    private val properties = mutableListOf<Any?>()

    fun id(id: Any) {
        this.id = id
    }

    fun bind(block: KotlinHolder.() -> Unit) {
        bind = block
    }

    fun bindIt(block: (KotlinHolder) -> Unit) {
        bind(block)
    }

    fun unbind(block: KotlinHolder.() -> Unit) {
        unbind = block
    }

    fun unbindIt(block: (KotlinHolder) -> Unit) {
        unbind(block)
    }

    fun view(block: (ViewGroup) -> View) {
        createView = block
    }

    fun layoutRes(id: Int) {
        if (!viewTypeSet) {
            viewType(id)
        }
        view {
            LayoutInflater.from(it.context).inflate(id, it, false)
        }
    }

    fun viewType(viewType: Int) {
        viewTypeSet = true
        this.viewType = viewType
    }

    fun properties(vararg properties: Any?) {
        this.properties.addAll(properties)
    }

    internal fun build(): DslItem = DslItem(id, bind, unbind, createView, properties, viewType!!)

}

fun ItemController.item(block: DslItemBuilder.() -> Unit): Item<*> =
    DslItemBuilder().apply(block).build().addTo(this)

fun ItemController.item(
    id: Any,
    layoutRes: Int,
    properties: Iterable<Any?>? = null,
    bind: KotlinHolder.() -> Unit
) = item {
    id(id)
    layoutRes(layoutRes)
    properties?.toList()?.toTypedArray()?.let { properties(*it) }
    bind(bind)
}

class DslItem internal constructor(
    id: Any,
    private val bind: ((KotlinHolder) -> Unit)? = null,
    private val unbind: ((KotlinHolder) -> Unit)? = null,
    private val createView: (ViewGroup) -> View,
    _properties: List<Any?>,
    override val viewType: Int
) : KotlinItem(id = id, properties = _properties) {

    override fun createView(parent: ViewGroup): View = createView.invoke(parent)

    override fun bind(holder: KotlinHolder) {
        super.bind(holder)
        bind?.invoke(holder)
    }

    override fun unbind(holder: KotlinHolder) {
        super.unbind(holder)
        unbind?.invoke(holder)
    }

}