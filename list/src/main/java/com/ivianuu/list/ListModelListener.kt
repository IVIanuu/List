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

    fun preCreateHolder(model: ListModel<*>) {
    }

    fun postCreateHolder(model: ListModel<*>, holder: ModelHolder) {
    }

    fun preCreateView(model: ListModel<*>) {
    }

    fun postCreateView(model: ListModel<*>, view: View) {
    }

    fun preBind(model: ListModel<*>, holder: ModelHolder) {
    }

    fun postBind(model: ListModel<*>, holder: ModelHolder) {
    }

    fun preUnbind(model: ListModel<*>, holder: ModelHolder) {
    }

    fun postUnbind(model: ListModel<*>, holder: ModelHolder) {
    }

    fun preAttach(model: ListModel<*>, holder: ModelHolder) {
    }

    fun postAttach(model: ListModel<*>, holder: ModelHolder) {
    }

    fun preDetach(model: ListModel<*>, holder: ModelHolder) {
    }

    fun postDetach(model: ListModel<*>, holder: ModelHolder) {
    }

    fun onFailedToRecycleView(model: ListModel<*>, holder: ModelHolder) {
    }

}

fun ListModel<*>.doOnPreCreateHolder(block: (model: ListModel<*>) -> Unit): ListModelListener =
    addListener(preCreateHolder = block)

fun ListModel<*>.doOnPreCreateHolder(block: (model: ListModel<*>, holder: ModelHolder) -> Unit): ListModelListener =
    addListener(postCreateHolder = block)

fun ListModel<*>.doOnPreCreateView(block: (model: ListModel<*>) -> Unit): ListModelListener =
    addListener(preCreateView = block)

fun ListModel<*>.doOnPostCreateView(block: (model: ListModel<*>, view: View) -> Unit): ListModelListener =
    addListener(postCreateView = block)

fun ListModel<*>.doOnPreBind(block: (model: ListModel<*>, holder: ModelHolder) -> Unit): ListModelListener =
    addListener(preBind = block)

fun ListModel<*>.doOnPostBind(block: (model: ListModel<*>, holder: ModelHolder) -> Unit): ListModelListener =
    addListener(postBind = block)

fun ListModel<*>.doOnPreUnbind(block: (model: ListModel<*>, holder: ModelHolder) -> Unit): ListModelListener =
    addListener(preUnbind = block)

fun ListModel<*>.doOnPostUnbind(block: (model: ListModel<*>, holder: ModelHolder) -> Unit): ListModelListener =
    addListener(postUnbind = block)

fun ListModel<*>.doOnPreAttach(block: (model: ListModel<*>, holder: ModelHolder) -> Unit): ListModelListener =
    addListener(preAttach = block)

fun ListModel<*>.doOnPostAttach(block: (model: ListModel<*>, holder: ModelHolder) -> Unit): ListModelListener =
    addListener(postAttach = block)

fun ListModel<*>.doOnPreDetach(block: (model: ListModel<*>, holder: ModelHolder) -> Unit): ListModelListener =
    addListener(preDetach = block)

fun ListModel<*>.doOnPostDetach(block: (model: ListModel<*>, holder: ModelHolder) -> Unit): ListModelListener =
    addListener(postDetach = block)

fun ListModel<*>.doOnFailedToRecycleView(block: (model: ListModel<*>, holder: ModelHolder) -> Unit): ListModelListener =
    addListener(onFailedToRecycleView = block)

fun ListModel<*>.addListener(
    preCreateHolder: ((model: ListModel<*>) -> Unit)? = null,
    postCreateHolder: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    preCreateView: ((model: ListModel<*>) -> Unit)? = null,
    postCreateView: ((model: ListModel<*>, view: View) -> Unit)? = null,
    preBind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    postBind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    preUnbind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    postUnbind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    preAttach: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    postAttach: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    preDetach: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    postDetach: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    onFailedToRecycleView: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null
): ListModelListener = ListModelListener(
    preCreateHolder, postCreateHolder,
    preCreateView, postCreateView,
    preBind, postBind,
    preUnbind, postUnbind,
    preAttach, postAttach,
    preDetach, postDetach,
    onFailedToRecycleView
).also(this::addListener)

fun ModelController.addModelListener(
    preCreateHolder: ((model: ListModel<*>) -> Unit)? = null,
    postCreateHolder: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    preCreateView: ((model: ListModel<*>) -> Unit)? = null,
    postCreateView: ((model: ListModel<*>, view: View) -> Unit)? = null,
    preBind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    postBind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    preUnbind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    postUnbind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    preAttach: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    postAttach: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    preDetach: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    postDetach: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    onFailedToRecycleView: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null
): ListModelListener = ListModelListener(
    preCreateHolder, postCreateHolder,
    preCreateView, postCreateView,
    preBind, postBind,
    preUnbind, postUnbind,
    preAttach, postAttach,
    preDetach, postDetach,
    onFailedToRecycleView
).also(this::addModelListener)

fun ListModelListener(
    preCreateHolder: ((model: ListModel<*>) -> Unit)? = null,
    postCreateHolder: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    preCreateView: ((model: ListModel<*>) -> Unit)? = null,
    postCreateView: ((model: ListModel<*>, view: View) -> Unit)? = null,
    preBind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    postBind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    preUnbind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    postUnbind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    preAttach: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    postAttach: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    preDetach: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    postDetach: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    onFailedToRecycleView: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null
): ListModelListener = LambdaListModelListener(
    preCreateHolder, postCreateHolder,
    preCreateView, postCreateView,
    preBind, postBind,
    preUnbind, postUnbind,
    preAttach, postAttach,
    preDetach, postDetach,
    onFailedToRecycleView
)

class LambdaListModelListener(
    private val preCreateHolder: ((model: ListModel<*>) -> Unit)? = null,
    private val postCreateHolder: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    private val preCreateView: ((model: ListModel<*>) -> Unit)? = null,
    private val postCreateView: ((model: ListModel<*>, view: View) -> Unit)? = null,
    private val preBind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    private val postBind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    private val preUnbind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    private val postUnbind: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    private val preAttach: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    private val postAttach: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    private val preDetach: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    private val postDetach: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null,
    private val onFailedToRecycleView: ((model: ListModel<*>, holder: ModelHolder) -> Unit)? = null
) : ListModelListener {

    override fun preCreateHolder(model: ListModel<*>) {
        preCreateHolder?.invoke(model)
    }

    override fun postCreateHolder(model: ListModel<*>, holder: ModelHolder) {
        postCreateHolder?.invoke(model, holder)
    }

    override fun preCreateView(model: ListModel<*>) {
        preCreateView?.invoke(model)
    }

    override fun postCreateView(model: ListModel<*>, view: View) {
        postCreateView?.invoke(model, view)
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

    override fun preAttach(model: ListModel<*>, holder: ModelHolder) {
        preAttach?.invoke(model, holder)
    }

    override fun postAttach(model: ListModel<*>, holder: ModelHolder) {
        postAttach?.invoke(model, holder)
    }

    override fun preDetach(model: ListModel<*>, holder: ModelHolder) {
        preDetach?.invoke(model, holder)
    }

    override fun postDetach(model: ListModel<*>, holder: ModelHolder) {
        postDetach?.invoke(model, holder)
    }

    override fun onFailedToRecycleView(model: ListModel<*>, holder: ModelHolder) {
        onFailedToRecycleView?.invoke(model, holder)
    }
}