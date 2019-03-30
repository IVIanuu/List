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

import android.os.Handler
import java.util.concurrent.Executor

private val delayedModelBuildHandler = Handler()

/**
 * Controller of a underlying [ModelAdapter]
 */
abstract class ModelController(
    private val diffingExecutor: Executor = ListPlugins.defaultDiffingExecutor,
    private val buildingExecutor: Executor = ListPlugins.defaultBuildingExecutor
) {

    open val adapter = ModelAdapter(diffingExecutor)

    var isBuildingModels = false
        private set

    private val currentModels = mutableListOf<ListModel<*>>()

    private var hasBuiltModelsEver = false

    private val buildModelsAction: () -> Unit = {
        cancelPendingModelBuild()

        isBuildingModels = true
        buildModels()
        isBuildingModels = false

        adapter.setModels(currentModels.toList())
        currentModels.clear()

        hasBuiltModelsEver = true
    }

    private val delayedModelBuildAction: () -> Unit = {
        buildingExecutor.execute(buildModelsAction)
    }

    private var requestedModelBuildType = RequestedModelBuildType.NONE

    /**
     * Requests a call to [buildModels]
     */
    open fun requestModelBuild() {
        check(!isBuildingModels) { "cannot call requestModelBuild() inside buildModels()" }
        if (hasBuiltModelsEver) {
            delayedModelBuildHandler.post(delayedModelBuildAction)
        } else {
            buildModelsAction()
        }
    }

    /**
     * Requests a synchronous call to [buildModels]
     */
    open fun requestImmediateModelBuild() {
        check(!isBuildingModels) { "cannot call requestImmediateModelBuild() inside buildModels()" }
        buildModelsAction()
    }

    /**
     * Enqueues a delayed call to [buildModels]
     */
    open fun requestDelayedModelBuild(delayMs: Long): Unit = synchronized(this) {
        check(!isBuildingModels) {
            "Cannot call requestDelayedModelBuild() from inside buildModels"
        }

        if (requestedModelBuildType == RequestedModelBuildType.DELAYED) {
            cancelPendingModelBuild()
        } else if (requestedModelBuildType == RequestedModelBuildType.NEXT_FRAME) {
            return@requestDelayedModelBuild
        }

        requestedModelBuildType =
            if (delayMs == 0L) RequestedModelBuildType.NEXT_FRAME else RequestedModelBuildType.DELAYED

        delayedModelBuildHandler.postDelayed(delayedModelBuildAction, delayMs)
    }

    /**
     * Cancels all pending calls to [buildModels]
     */
    fun cancelPendingModelBuild(): Unit = synchronized(this) {
        if (requestedModelBuildType != RequestedModelBuildType.NONE) {
            requestedModelBuildType = RequestedModelBuildType.NONE
            delayedModelBuildHandler.removeCallbacks(delayedModelBuildAction)
        }
    }

    /**
     * Builds the list of models and adds them via [add]
     */
    protected abstract fun buildModels()

    /**
     * Adds all [models]
     */
    protected fun add(models: Iterable<ListModel<*>>) {
        models.forEach(this::add)
    }

    /**
     * Adds all [models]
     */
    protected fun add(vararg models: ListModel<*>) {
        models.forEach(this::add)
    }

    private fun add(model: ListModel<*>) {
        checkBuildingModels()
        currentModels.add(model)
    }

    @PublishedApi
    internal fun addInternal(model: ListModel<*>) {
        add(model)
    }

    /**
     * Adds the [listener] to all [ListModel]s
     */
    fun addModelListener(listener: ListModelListener) {
        adapter.addModelListener(listener)
    }

    /**
     * Removes the previously added [listener]
     */
    fun removeModelListener(listener: ListModelListener) {
        adapter.removeModelListener(listener)
    }

    inline operator fun <T : ListModel<*>> ListModelFactory<T>.invoke(
        body: T.() -> Unit
    ): T = create().apply(body).addTo(this@ModelController)

    inline operator fun <T : ListModel<*>> T.invoke(body: T.() -> Unit): T =
        apply(body).addTo(this@ModelController)

    inline fun <T : ListModel<*>> T.add(body: T.() -> Unit): T =
        apply(body).addTo(this@ModelController)

    private fun checkBuildingModels() {
        check(isBuildingModels) { "cannot add models outside of buildModels()" }
    }

    private enum class RequestedModelBuildType {
        NONE, NEXT_FRAME, DELAYED
    }

}