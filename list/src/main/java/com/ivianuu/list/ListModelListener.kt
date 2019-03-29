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

/**
 * Listener for [ListModel]s
 */
interface ListModelListener {

    fun onCreateHolder(model: ListModel<*>, holder: ModelHolder) {
    }

    fun onBuildView(model: ListModel<*>, view: View) {
    }

    fun preBind(model: ListModel<*>, holder: ModelHolder) {
    }

    fun postBind(model: ListModel<*>, holder: ModelHolder) {
    }

    fun preUnbind(model: ListModel<*>, holder: ModelHolder) {
    }

    fun postUnbind(model: ListModel<*>, holder: ModelHolder) {
    }

}

fun ListModel<*>.doOnCreateHolder(block: (model: ListModel<*>, holder: ModelHolder) -> Unit): ListModelListener =
    addListener(onCreateHolder = block)

fun ListModel<*>.doOnBuildView(block: (model: ListModel<*>, view: View) -> Unit): ListModelListener =
    addListener(onBuildView = block)

fun ListModel<*>.doOnPreBind(block: (model: ListModel<*>, holder: ModelHolder) -> Unit): ListModelListener =
    addListener(preBind = block)

fun ListModel<*>.doOnPostBind(block: (model: ListModel<*>, holder: ModelHolder) -> Unit): ListModelListener =
    addListener(postBind = block)

fun ListModel<*>.doOnPreUnbind(block: (model: ListModel<*>, holder: ModelHolder) -> Unit): ListModelListener =
    addListener(preUnbind = block)

fun ListModel<*>.doOnPostUnbind(block: (model: ListModel<*>, holder: ModelHolder) -> Unit): ListModelListener =
    addListener(postUnbind = block)

fun ListModel<*>.addListener(
    onCreateHolder: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    onBuildView: ((model: ListModel<*>, view: View) -> Unit)? = null,
    preBind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    postBind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    preUnbind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    postUnbind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null
): ListModelListener = ListModelListener(
    onCreateHolder,
    onBuildView,
    preBind, postBind,
    preUnbind, postUnbind
).also(this::addListener)

fun ModelAdapter.addModelListener(
    onCreateHolder: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    onBuildView: ((model: ListModel<*>, view: View) -> Unit)? = null,
    preBind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    postBind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    preUnbind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    postUnbind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null
): ListModelListener = ListModelListener(
    onCreateHolder,
    onBuildView,
    preBind, postBind,
    preUnbind, postUnbind
).also(this::addModelListener)

fun ModelController.addModelListener(
    onCreateHolder: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    onBuildView: ((model: ListModel<*>, view: View) -> Unit)? = null,
    preBind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    postBind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    preUnbind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    postUnbind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null
): ListModelListener = ListModelListener(
    onCreateHolder,
    onBuildView,
    preBind, postBind,
    preUnbind, postUnbind
).also(this::addModelListener)

fun ListModelListener(
    onCreateHolder: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    onBuildView: ((model: ListModel<*>, view: View) -> Unit)? = null,
    preBind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    postBind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    preUnbind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    postUnbind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null
): ListModelListener = LambdaListModelListener(
    onCreateHolder,
    onBuildView,
    preBind, postBind,
    preUnbind, postUnbind
)

class LambdaListModelListener(
    private val onCreateHolder: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    private val onBuildView: ((model: ListModel<*>, view: View) -> Unit)? = null,
    private val preBind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    private val postBind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    private val preUnbind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    private val postUnbind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null
) : ListModelListener {

    override fun onCreateHolder(model: ListModel<*>, holder: ModelHolder) {
        onCreateHolder?.invoke(model, holder)
    }

    override fun onBuildView(model: ListModel<*>, view: View) {
        onBuildView?.invoke(model, view)
    }

    override fun preBind(model: ListModel<*>, holder: ModelHolder) {
        preBind?.invoke(model, holder)
    }

    override fun postBind(model: ListModel<*>, holder: ModelHolder) {
        postBind?.invoke(model, holder)
    }

    override fun preUnbind(model: ListModel<*>, holder: ModelHolder) {
        preUnbind?.invoke(model, holder)
    }

    override fun postUnbind(model: ListModel<*>, holder: ModelHolder) {
        postUnbind?.invoke(model, holder)
    }

}