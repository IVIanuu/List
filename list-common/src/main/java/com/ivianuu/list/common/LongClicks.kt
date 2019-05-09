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

/**
/**
 * [View] on long click listener
 */
typealias LongClickListener = (View) -> Boolean

/**
 * Returns [ItemEvents] for view long clicks
 */
fun <I : Item<H>, H : Holder> I.longClicks(
    viewProvider: (H) -> View = { it.view }
): Lazy<ItemEvents<LongClickListener>> = lazy(LazyThreadSafetyMode.NONE) {
    ItemLongClicks(this, viewProvider)
}


/**
 * Returns [ItemEvents] for long clicks
 */
fun <I : Item<H>, H : Holder> I.longClicks(
    viewId: Int
): Lazy<ItemEvents<LongClickListener>> = lazy(LazyThreadSafetyMode.NONE) {
    ItemLongClicks(this) { it.view.findViewById(viewId) }
}

private class ItemLongClicks<T : Item<H>, H : Holder>(
    private val item: T,
    private val viewProvider: (H) -> View = { it.view }
) : ItemEvents<(View) -> Boolean> {

    private var _callback: ((View) -> Boolean)? = null

    init {
        item.addListener(
            postBind = { _, holder -> viewProvider(holder as H).setOnLongClickListener(_callback) },
            preUnbind = { _, holder -> viewProvider(holder as H).setOnLongClickListener(null) }
        )
    }

    override fun setCallback(callback: (View) -> Boolean) {
        _callback = callback
    }
}*/