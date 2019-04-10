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
 * List adapter for [Item]s
 */
open class ItemAdapter : RecyclerView.Adapter<ItemViewHolder>() {

    private val differ = AsyncItemDiffer { it.dispatchTo(this) }

    /**
     * All current items
     */
    val currentItems: List<Item<*>> get() = differ.currentList

    internal val itemListeners get() = _itemListeners
    private val _itemListeners = mutableSetOf<ItemListener>()

    private var lastItemForViewTypeLookup: Item<*>? = null

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        var item = lastItemForViewTypeLookup
        if (item == null || item.viewType != viewType) {
            item = currentItems.first { it.viewType == viewType }
        }
        val view = item.createView(parent)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = currentItems[position]
        holder.bind(item)
    }

    override fun onViewRecycled(holder: ItemViewHolder) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    override fun getItemId(position: Int): Long = currentItems[position].id

    override fun getItemCount(): Int = currentItems.size

    override fun getItemViewType(position: Int): Int {
        val item = currentItems[position]
        lastItemForViewTypeLookup = item
        return item.viewType
    }

    final override fun setHasStableIds(hasStableIds: Boolean) {
        require(hasStableIds) { "This implementation relies on stable ids" }
        super.setHasStableIds(hasStableIds)
    }

    /**
     * Replaces the current items with [items], diffs them and dispatches the changes to
     * this adapter
     */
    fun setItems(items: List<Item<*>>) {
        items.checkDuplicates()
        items.forEach { it.addedToAdapter(this) }
        differ.submitList(items)
    }

    /**
     * Replaces the current items with [items] but doesn't perform any diffing
     * And does not notify any changes to this adapter
     */
    fun overrideItems(items: List<Item<*>>) {
        items.checkDuplicates()
        items.forEach { it.addedToAdapter(this) }
        differ.forceListOverride(items)
    }

    /**
     * Adds the [listener] to all [Item]s
     */
    fun addItemListener(listener: ItemListener): Closeable {
        _itemListeners.add(listener)
        return Closeable { removeItemListener(listener) }
    }

    /**
     * Removes the previously added [listener]
     */
    fun removeItemListener(listener: ItemListener) {
        _itemListeners.remove(listener)
    }

    private fun List<Item<*>>.checkDuplicates() {
        // check for duplicated ids
        groupBy(Item<*>::id)
            .filterValues { it.size > 1 }
            .forEach {
                error("Duplicated id ${it.value}")
            }
    }
}

/**
 * Returns the item at [index]
 */
fun ItemAdapter.getItemAt(index: Int): Item<*> = currentItems[index]

/**
 * Returns the index of the [item]
 */
fun ItemAdapter.indexOfItem(item: Item<*>): Int = currentItems.indexOf(item)

/**
 * Adds the [item]
 */
fun ItemAdapter.addItem(item: Item<*>) {
    val newItems = currentItems.toMutableList()
    newItems.add(item)
    overrideItems(newItems)
    notifyItemInserted(newItems.lastIndex)
}

/**
 * Adds the [item] at the [index]
 */
fun ItemAdapter.addItem(index: Int, item: Item<*>) {
    val newItems = currentItems.toMutableList()
    newItems.add(index, item)
    overrideItems(newItems)
    notifyItemInserted(index)
}

/**
 * Adds all [items]
 */
fun ItemAdapter.addItems(vararg items: Item<*>) {
    val newItems = items.toMutableList()
    val startIndex = newItems.lastIndex
    newItems.addAll(items)
    overrideItems(newItems)
    notifyItemRangeInserted(startIndex, items.size)
}

/**
 * Adds all [items] at the [index]
 */
fun ItemAdapter.addItems(index: Int, vararg items: Item<*>) {
    val newItems = items.toMutableList()
    newItems.addAll(index, items.asList())
    setItems(newItems)
    notifyItemRangeInserted(index, items.size)
}

/**
 * Adds all [items]
 */
fun ItemAdapter.addItems(items: Iterable<Item<*>>) {
    val newItems = items.toMutableList()
    val startIndex = newItems.lastIndex
    newItems.addAll(items)
    overrideItems(newItems)
    notifyItemRangeInserted(startIndex, items.count())
}

/**
 * Adds all [items] at the [index]
 */
fun ItemAdapter.addItems(index: Int, items: Iterable<Item<*>>) {
    val newItems = items.toMutableList()
    newItems.addAll(index, items.toList())
    overrideItems(newItems)
    notifyItemRangeInserted(index, items.count())
}

/**
 * Removes the [item] if added
 */
fun ItemAdapter.removeItem(item: Item<*>) {
    val newItems = currentItems.toMutableList()
    val index = newItems.indexOf(item)
    if (index != -1) {
        newItems.removeAt(index)
        overrideItems(newItems)
        notifyItemRemoved(index)
    }
}

/**
 * Removes the item at the [index]
 */
fun ItemAdapter.removeItemAt(index: Int) {
    val newItems = currentItems.toMutableList()
    newItems.removeAt(index)
    overrideItems(newItems)
    notifyItemRemoved(index)
}

/**
 * Moves the the item at [from] to the [to] index
 */
fun ItemAdapter.moveItem(from: Int, to: Int) {
    val newItems = currentItems.toMutableList()
    Collections.swap(newItems, from, to)
    overrideItems(newItems)
    notifyItemMoved(from, to)
}

/**
 * Clears all added items
 */
fun ItemAdapter.clearItems() {
    val oldItems = currentItems
    overrideItems(emptyList())
    notifyItemRangeRemoved(0, oldItems.size)
}