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

package com.ivianuu.list.common

import android.view.View
import com.ivianuu.list.Holder
import com.ivianuu.list.Item
import com.ivianuu.list.addListener

/**
 * Notifies the [block] on each click to the result of [viewProvider]
 */
fun <T : Item<H>, H : Holder> T.onClick(
    viewProvider: (H) -> View = { it.view },
    block: (item: T, view: View) -> Unit
) {
    val listener = View.OnClickListener { block(this, it) }
    addListener(
        postBind = { _, holder -> viewProvider(holder as H).setOnClickListener(listener) },
        preUnbind = { _, holder -> viewProvider(holder as H).setOnClickListener(null) }
    )
}

/**
 * Notifies the [block] on each click on the view with the [viewId]
 */
fun <T : Item<H>, H : Holder> T.onClick(
    viewId: Int,
    block: (item: T, view: View) -> Unit
) {
    onClick({ it.view.findViewById(viewId) }, block)
}

/**
 * Notifies the [block] on each long click to the result of [viewProvider]
 */
fun <T : Item<H>, H : Holder> T.onLongClick(
    viewProvider: (H) -> View = { it.view },
    block: (item: T, view: View) -> Boolean
) {
    val listener = View.OnLongClickListener { block(this, it) }
    addListener(
        postBind = { _, holder -> viewProvider(holder as H).setOnLongClickListener(listener) },
        preUnbind = { _, holder -> viewProvider(holder as H).setOnLongClickListener(null) }
    )
}

/**
 * Notifies the [block] on each long click on the view with the [viewId]
 */
fun <T : Item<H>, H : Holder> T.onLongClick(
    viewId: Int,
    block: (item: T, view: View) -> Boolean
) {
    onLongClick({ it.view.findViewById(viewId) }, block)
}