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

import android.view.View
import com.ivianuu.closeable.Closeable

/**
 * Listener for [Item]s
 */
interface ItemListener {

    fun onCreateHolder(item: Item<*>, holder: Holder) {
    }

    fun onBuildView(item: Item<*>, view: View) {
    }

    fun preBind(item: Item<*>, holder: Holder) {
    }

    fun postBind(item: Item<*>, holder: Holder) {
    }

    fun preUnbind(item: Item<*>, holder: Holder) {
    }

    fun postUnbind(item: Item<*>, holder: Holder) {
    }

}

fun Item<*>.doOnCreateHolder(block: (item: Item<*>, holder: Holder) -> Unit): Closeable =
    addListener(onCreateHolder = block)

fun Item<*>.doOnBuildView(block: (item: Item<*>, view: View) -> Unit): Closeable =
    addListener(onBuildView = block)

fun Item<*>.doOnPreBind(block: (item: Item<*>, holder: Holder) -> Unit): Closeable =
    addListener(preBind = block)

fun Item<*>.doOnPostBind(block: (item: Item<*>, holder: Holder) -> Unit): Closeable =
    addListener(postBind = block)

fun Item<*>.doOnPreUnbind(block: (item: Item<*>, holder: Holder) -> Unit): Closeable =
    addListener(preUnbind = block)

fun Item<*>.doOnPostUnbind(block: (item: Item<*>, holder: Holder) -> Unit): Closeable =
    addListener(postUnbind = block)

fun Item<*>.addListener(
    onCreateHolder: ((item: Item<*>, holder: Holder) -> Unit)? = null,
    onBuildView: ((item: Item<*>, view: View) -> Unit)? = null,
    preBind: ((item: Item<*>, holder: Holder) -> Unit)? = null,
    postBind: ((item: Item<*>, holder: Holder) -> Unit)? = null,
    preUnbind: ((item: Item<*>, holder: Holder) -> Unit)? = null,
    postUnbind: ((item: Item<*>, holder: Holder) -> Unit)? = null
): Closeable = ItemListener(
    onCreateHolder,
    onBuildView,
    preBind, postBind,
    preUnbind, postUnbind
).let { addListener(it) }

fun ItemAdapter.addItemListener(
    onCreateHolder: ((item: Item<*>, holder: Holder) -> Unit)? = null,
    onBuildView: ((item: Item<*>, view: View) -> Unit)? = null,
    preBind: ((item: Item<*>, holder: Holder) -> Unit)? = null,
    postBind: ((item: Item<*>, holder: Holder) -> Unit)? = null,
    preUnbind: ((item: Item<*>, holder: Holder) -> Unit)? = null,
    postUnbind: ((item: Item<*>, holder: Holder) -> Unit)? = null
): Closeable = ItemListener(
    onCreateHolder,
    onBuildView,
    preBind, postBind,
    preUnbind, postUnbind
).let { addItemListener(it) }

fun ItemController.addItemListener(
    onCreateHolder: ((item: Item<*>, holder: Holder) -> Unit)? = null,
    onBuildView: ((item: Item<*>, view: View) -> Unit)? = null,
    preBind: ((item: Item<*>, holder: Holder) -> Unit)? = null,
    postBind: ((item: Item<*>, holder: Holder) -> Unit)? = null,
    preUnbind: ((item: Item<*>, holder: Holder) -> Unit)? = null,
    postUnbind: ((item: Item<*>, holder: Holder) -> Unit)? = null
): Closeable = ItemListener(
    onCreateHolder,
    onBuildView,
    preBind, postBind,
    preUnbind, postUnbind
).let { addItemListener(it) }

fun ItemListener(
    onCreateHolder: ((item: Item<*>, holder: Holder) -> Unit)? = null,
    onBuildView: ((item: Item<*>, view: View) -> Unit)? = null,
    preBind: ((item: Item<*>, holder: Holder) -> Unit)? = null,
    postBind: ((item: Item<*>, holder: Holder) -> Unit)? = null,
    preUnbind: ((item: Item<*>, holder: Holder) -> Unit)? = null,
    postUnbind: ((item: Item<*>, holder: Holder) -> Unit)? = null
): ItemListener = LambdaItemListener(
    onCreateHolder,
    onBuildView,
    preBind, postBind,
    preUnbind, postUnbind
)

class LambdaItemListener(
    private val onCreateHolder: ((item: Item<*>, holder: Holder) -> Unit)? = null,
    private val onBuildView: ((item: Item<*>, view: View) -> Unit)? = null,
    private val preBind: ((item: Item<*>, holder: Holder) -> Unit)? = null,
    private val postBind: ((item: Item<*>, holder: Holder) -> Unit)? = null,
    private val preUnbind: ((item: Item<*>, holder: Holder) -> Unit)? = null,
    private val postUnbind: ((item: Item<*>, holder: Holder) -> Unit)? = null
) : ItemListener {

    override fun onCreateHolder(item: Item<*>, holder: Holder) {
        onCreateHolder?.invoke(item, holder)
    }

    override fun onBuildView(item: Item<*>, view: View) {
        onBuildView?.invoke(item, view)
    }

    override fun preBind(item: Item<*>, holder: Holder) {
        preBind?.invoke(item, holder)
    }

    override fun postBind(item: Item<*>, holder: Holder) {
        postBind?.invoke(item, holder)
    }

    override fun preUnbind(item: Item<*>, holder: Holder) {
        preUnbind?.invoke(item, holder)
    }

    override fun postUnbind(item: Item<*>, holder: Holder) {
        postUnbind?.invoke(item, holder)
    }

}