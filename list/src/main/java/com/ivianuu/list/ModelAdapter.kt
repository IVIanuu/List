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
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import java.util.concurrent.Executor

/**
 * List adapter for [ListModel]s
 */
open class ModelAdapter(
    private val controller: ModelController,
    diffingExecutor: Executor
) : RecyclerView.Adapter<ModelViewHolder>() {

    private val helper = AsyncListDiffer<ListModel<*>>(
        AdapterListUpdateCallback(this),
        AsyncDifferConfig.Builder(DIFF_CALLBACK)
            .setBackgroundThreadExecutor(diffingExecutor)
            .build()
    )

    /**
     * All current models
     */
    val models: List<ListModel<*>> get() = helper.currentList

    internal val modelListeners get() = _modelListeners
    private val _modelListeners = mutableSetOf<ListModelListener>()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
        val model = models.first { it.viewType == viewType }
        val view = model.buildView(parent)
        return ModelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModelViewHolder, position: Int) {
    }

    override fun onBindViewHolder(
        holder: ModelViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)
        val model = models[position]
        holder.bind(model)
    }

    override fun onViewRecycled(holder: ModelViewHolder) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    override fun getItemId(position: Int): Long = models[position].id

    override fun getItemCount(): Int = models.size

    override fun getItemViewType(position: Int): Int = models[position].viewType

    override fun onViewAttachedToWindow(holder: ModelViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.requireModel().attach(holder.holder)
    }

    override fun onViewDetachedFromWindow(holder: ModelViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.requireModel().detach(holder.holder)
    }

    override fun onFailedToRecycleView(holder: ModelViewHolder): Boolean =
        holder.requireModel().failedToRecycleView(holder.holder)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        controller.attachedToRecyclerView(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        controller.detachedFromRecyclerView(recyclerView)
    }

    final override fun setHasStableIds(hasStableIds: Boolean) {
        require(hasStableIds) { "This implementation relies on stable ids" }
        super.setHasStableIds(hasStableIds)
    }

    fun setModels(models: List<ListModel<*>>) {
        helper.submitList(models.toList())
    }

    fun addModelListener(listener: ListModelListener) {
        _modelListeners.add(listener)
    }

    fun removeModelListener(listener: ListModelListener) {
        _modelListeners.remove(listener)
    }

    private companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListModel<*>>() {
            override fun areItemsTheSame(oldItem: ListModel<*>, newItem: ListModel<*>): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ListModel<*>, newItem: ListModel<*>): Boolean =
                oldItem == newItem
        }
    }
}

fun ModelAdapter.getModelAt(index: Int): ListModel<*> = models[index]

fun ModelAdapter.indexOfModel(model: ListModel<*>): Int = models.indexOf(model)

fun ModelAdapter.addModel(model: ListModel<*>) {
    val newModels = models.toMutableList()
    newModels.add(model)
    setModels(newModels)
}

fun ModelAdapter.addModel(index: Int, model: ListModel<*>) {
    val newModels = models.toMutableList()
    newModels.add(index, model)
    setModels(newModels)
}

fun ModelAdapter.addModels(vararg models: ListModel<*>) {
    val newModels = models.toMutableList()
    newModels.addAll(models)
    setModels(newModels)
}

fun ModelAdapter.addModels(index: Int, vararg models: ListModel<*>) {
    val newModels = models.toMutableList()
    newModels.addAll(index, models.asList())
    setModels(newModels)
}

fun ModelAdapter.addModels(models: Iterable<ListModel<*>>) {
    val newModels = models.toMutableList()
    newModels.addAll(models)
    setModels(newModels)
}

fun ModelAdapter.addModels(index: Int, models: Iterable<ListModel<*>>) {
    val newModels = models.toMutableList()
    newModels.addAll(index, models.toList())
    setModels(newModels)
}

fun ModelAdapter.removeModel(model: ListModel<*>) {
    val newModels = models.toMutableList()
    newModels.remove(model)
    setModels(newModels)
}

fun ModelAdapter.moveModel(from: Int, to: Int) {
    val newModels = models.toMutableList()
    Collections.swap(newModels, from, to)
    setModels(newModels)
}

fun ModelAdapter.clearModels() {
    setModels(emptyList())
}