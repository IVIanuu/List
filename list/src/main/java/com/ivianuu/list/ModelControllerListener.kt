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

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView

/**
 * @author Manuel Wrage (IVIanuu)
 */
interface ModelControllerListener {

    fun onAttachedToRecyclerView(controller: ModelController, recyclerView: RecyclerView) {
    }

    fun onDetachedFromRecyclerView(controller: ModelController, recyclerView: RecyclerView) {
    }

    fun onInterceptBuildModels(controller: ModelController, models: MutableList<ListModel<*>>) {
    }

    fun onModelsBuildResult(controller: ModelController, result: DiffResult) {
    }

    fun onRestoreState(controller: ModelController, savedState: Bundle) {
    }

    fun onSaveState(controller: ModelController, savedState: Bundle) {
    }


}

fun ModelController.doOnAttachedToRecyclerView(
    block: (controller: ModelController, recyclerView: RecyclerView) -> Unit
): ModelControllerListener = addListener(onAttachedToRecyclerView = block)

fun ModelController.doOnDetachedFromRecyclerView(
    block: (controller: ModelController, recyclerView: RecyclerView) -> Unit
): ModelControllerListener = addListener(onDetachedFromRecyclerView = block)

fun ModelController.doOnInterceptBuildModels(
    block: (controller: ModelController, models: MutableList<ListModel<*>>) -> Unit
): ModelControllerListener = addListener(onInterceptBuildModels = block)

fun ModelController.doOnModelsBuildResult(
    block: (controller: ModelController, result: DiffResult) -> Unit
): ModelControllerListener = addListener(onModelsBuildResult = block)

fun ModelController.doOnRestoreState(
    block: (controller: ModelController, savedState: Bundle) -> Unit
): ModelControllerListener = addListener(onRestoreState = block)

fun ModelController.doOnSaveState(
    block: (controller: ModelController, savedState: Bundle) -> Unit
): ModelControllerListener = addListener(onSaveState = block)

fun ModelController.addListener(
    onAttachedToRecyclerView: ((controller: ModelController, recyclerView: RecyclerView) -> Unit)? = null,
    onDetachedFromRecyclerView: ((controller: ModelController, recyclerView: RecyclerView) -> Unit)? = null,
    onInterceptBuildModels: ((controller: ModelController, models: MutableList<ListModel<*>>) -> Unit)? = null,
    onModelsBuildResult: ((controller: ModelController, result: DiffResult) -> Unit)? = null,
    onRestoreState: ((controller: ModelController, savedState: Bundle) -> Unit)? = null,
    onSaveState: ((controller: ModelController, savedState: Bundle) -> Unit)? = null
): ModelControllerListener = ListControllerListener(
    onAttachedToRecyclerView, onDetachedFromRecyclerView,
    onInterceptBuildModels,
    onModelsBuildResult,
    onRestoreState, onSaveState
).also(this::addListener)

fun ListControllerListener(
    onAttachedToRecyclerView: ((controller: ModelController, recyclerView: RecyclerView) -> Unit)? = null,
    onDetachedFromRecyclerView: ((controller: ModelController, recyclerView: RecyclerView) -> Unit)? = null,
    onInterceptBuildModels: ((controller: ModelController, models: MutableList<ListModel<*>>) -> Unit)? = null,
    onModelsBuildResult: ((controller: ModelController, result: DiffResult) -> Unit)? = null,
    onRestoreState: ((controller: ModelController, savedState: Bundle) -> Unit)? = null,
    onSaveState: ((controller: ModelController, savedState: Bundle) -> Unit)? = null
): ModelControllerListener = LambdaModelControllerListener(
    onAttachedToRecyclerView, onDetachedFromRecyclerView,
    onInterceptBuildModels,
    onModelsBuildResult,
    onRestoreState, onSaveState
)

class LambdaModelControllerListener(
    private val onAttachedToRecyclerView: ((controller: ModelController, recyclerView: RecyclerView) -> Unit)? = null,
    private val onDetachedFromRecyclerView: ((controller: ModelController, recyclerView: RecyclerView) -> Unit)? = null,
    private val onInterceptBuildModels: ((controller: ModelController, models: MutableList<ListModel<*>>) -> Unit)? = null,
    private val onModelsBuildResult: ((controller: ModelController, result: DiffResult) -> Unit)? = null,
    private val onRestoreState: ((controller: ModelController, savedState: Bundle) -> Unit)? = null,
    private val onSaveState: ((controller: ModelController, savedState: Bundle) -> Unit)? = null
) : ModelControllerListener {

    override fun onAttachedToRecyclerView(controller: ModelController, recyclerView: RecyclerView) {
        onAttachedToRecyclerView?.invoke(controller, recyclerView)
    }

    override fun onDetachedFromRecyclerView(
        controller: ModelController,
        recyclerView: RecyclerView
    ) {
        onDetachedFromRecyclerView?.invoke(controller, recyclerView)
    }

    override fun onInterceptBuildModels(
        controller: ModelController,
        models: MutableList<ListModel<*>>
    ) {
        onInterceptBuildModels?.invoke(controller, models)
    }

    override fun onModelsBuildResult(controller: ModelController, result: DiffResult) {
        onModelsBuildResult?.invoke(controller, result)
    }

    override fun onRestoreState(controller: ModelController, savedState: Bundle) {
        onRestoreState?.invoke(controller, savedState)
    }

    override fun onSaveState(controller: ModelController, savedState: Bundle) {
        onSaveState?.invoke(controller, savedState)
    }

}