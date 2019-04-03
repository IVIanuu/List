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

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ivianuu.closeable.Closeable
import java.util.*

/**
 * List adapter for [ListModel]s
 */
open class ModelAdapter : RecyclerView.Adapter<ModelViewHolder>() {

    private val differ = AsyncModelDiffer { it.dispatchTo(this) }

    /**
     * All current models
     */
    val currentModels: List<ListModel<*>> get() = differ.currentList

    internal val modelListeners get() = _modelListeners
    private val _modelListeners = mutableSetOf<ListModelListener>()

    private var lastModelForViewTypeLookup: ListModel<*>? = null

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
        var model = lastModelForViewTypeLookup
        if (model == null || model.viewType != viewType) {
            model = currentModels.first { it.viewType == viewType }
        }
        val view = model.createView(parent)
        return ModelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModelViewHolder, position: Int) {
        val model = currentModels[position]
        holder.bind(model)
    }

    override fun onViewRecycled(holder: ModelViewHolder) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    override fun getItemId(position: Int): Long = currentModels[position].id

    override fun getItemCount(): Int = currentModels.size

    override fun getItemViewType(position: Int): Int {
        val model = currentModels[position]
        lastModelForViewTypeLookup = model
        return model.viewType
    }

    final override fun setHasStableIds(hasStableIds: Boolean) {
        require(hasStableIds) { "This implementation relies on stable ids" }
        super.setHasStableIds(hasStableIds)
    }

    /**
     * Replaces the current models with [models], diffs them and dispatches the changes to
     * this adapter
     */
    fun setModels(models: List<ListModel<*>>) {
        models.checkDuplicates()
        models.forEach { it.addedToAdapter(this) }
        differ.submitList(models)
    }

    /**
     * Replaces the current models with [models] but doesn't perform any diffing
     * And does not notify any changes to this adapter
     */
    fun overrideModels(models: List<ListModel<*>>) {
        models.checkDuplicates()
        models.forEach { it.addedToAdapter(this) }
        differ.forceListOverride(models)
    }

    /**
     * Adds the [listener] to all [ListModel]s
     */
    fun addModelListener(listener: ListModelListener): Closeable {
        _modelListeners.add(listener)
        return Closeable { removeModelListener(listener) }
    }

    /**
     * Removes the previously added [listener]
     */
    fun removeModelListener(listener: ListModelListener) {
        _modelListeners.remove(listener)
    }

    private fun List<ListModel<*>>.checkDuplicates() {
        // check for duplicated ids
        groupBy(ListModel<*>::id)
            .filterValues { it.size > 1 }
            .forEach {
                error("Duplicated id ${it.value}")
            }
    }
}

/**
 * Returns the model at [index]
 */
fun ModelAdapter.getModelAt(index: Int): ListModel<*> = currentModels[index]

/**
 * Returns the index of the [model]
 */
fun ModelAdapter.indexOfModel(model: ListModel<*>): Int = currentModels.indexOf(model)

/**
 * Adds the [model]
 */
fun ModelAdapter.addModel(model: ListModel<*>) {
    val newModels = currentModels.toMutableList()
    newModels.add(model)
    overrideModels(newModels)
    notifyItemInserted(newModels.lastIndex)
}

/**
 * Adds the [model] at the [index]
 */
fun ModelAdapter.addModel(index: Int, model: ListModel<*>) {
    val newModels = currentModels.toMutableList()
    newModels.add(index, model)
    overrideModels(newModels)
    notifyItemInserted(index)
}

/**
 * Adds all [models]
 */
fun ModelAdapter.addModels(vararg models: ListModel<*>) {
    val newModels = models.toMutableList()
    val startIndex = newModels.lastIndex
    newModels.addAll(models)
    overrideModels(newModels)
    notifyItemRangeInserted(startIndex, models.size)
}

/**
 * Adds all [models] at the [index]
 */
fun ModelAdapter.addModels(index: Int, vararg models: ListModel<*>) {
    val newModels = models.toMutableList()
    newModels.addAll(index, models.asList())
    setModels(newModels)
    notifyItemRangeInserted(index, models.size)
}

/**
 * Adds all [models]
 */
fun ModelAdapter.addModels(models: Iterable<ListModel<*>>) {
    val newModels = models.toMutableList()
    val startIndex = newModels.lastIndex
    newModels.addAll(models)
    overrideModels(newModels)
    notifyItemRangeInserted(startIndex, models.count())
}

/**
 * Adds all [models] at the [index]
 */
fun ModelAdapter.addModels(index: Int, models: Iterable<ListModel<*>>) {
    val newModels = models.toMutableList()
    newModels.addAll(index, models.toList())
    overrideModels(newModels)
    notifyItemRangeInserted(index, models.count())
}

/**
 * Removes the [model] if added
 */
fun ModelAdapter.removeModel(model: ListModel<*>) {
    val newModels = currentModels.toMutableList()
    val index = newModels.indexOf(model)
    if (index != -1) {
        newModels.removeAt(index)
        overrideModels(newModels)
        notifyItemRemoved(index)
    }
}

/**
 * Removes the model at the [index]
 */
fun ModelAdapter.removeModelAt(index: Int) {
    val newModels = currentModels.toMutableList()
    newModels.removeAt(index)
    overrideModels(newModels)
    notifyItemRemoved(index)
}

/**
 * Moves the the model at [from] to the [to] index
 */
fun ModelAdapter.moveModel(from: Int, to: Int) {
    val newModels = currentModels.toMutableList()
    Collections.swap(newModels, from, to)
    overrideModels(newModels)
    notifyItemMoved(from, to)
}

/**
 * Clears all added models
 */
fun ModelAdapter.clearModels() {
    val oldModels = currentModels
    overrideModels(emptyList())
    notifyItemRangeRemoved(0, oldModels.size)
}