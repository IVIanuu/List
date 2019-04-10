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

import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.ivianuu.list.*
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
abstract class ItemTouchCallback<T : Item<*>>(
    private val targetItemClass: KClass<T>? = null
) : ItemTouchHelper.Callback(), ItemDragCallback<T>,
    ItemSwipeCallback<T> {

    private var holderBeingDragged: ItemViewHolder? = null
    private var holderBeingSwiped: ItemViewHolder? = null

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        viewHolder as ItemViewHolder

        val item = viewHolder.requireItem()

        // If multiple touch callbacks are registered on the recyclerview (to support combinations of
        // dragging and dropping) then we won't want to enable anything if another
        // callback has a view actively selected.
        val isOtherCallbackActive = holderBeingDragged == null
                && holderBeingSwiped == null
                && recyclerView.hasSelection

        return if (!isOtherCallbackActive && isTouchableItem(item)) {
            getMovementFlagsForItem(item as T, viewHolder.adapterPosition)
        } else {
            0
        }
    }

    override fun canDropOver(
        recyclerView: RecyclerView,
        current: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        current as ItemViewHolder
        target as ItemViewHolder

        // By default we don't allow dropping on a item that isn't a drag target
        return isTouchableItem(target.requireItem())
    }

    protected open fun isTouchableItem(item: Item<*>): Boolean =
        targetItemClass?.java?.isInstance(item) ?: false

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        viewHolder as ItemViewHolder

        val fromPosition = viewHolder.adapterPosition
        val toPosition = target.adapterPosition
        (recyclerView.adapter as ItemAdapter).moveItem(fromPosition, toPosition)

        val item = viewHolder.requireItem()
        check(isTouchableItem(item)) {
            "A item was dragged that is not a valid target: ${item.javaClass}"
        }

        onItemMoved(fromPosition, toPosition, item as T, viewHolder.itemView)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        viewHolder as ItemViewHolder

        val item = viewHolder.requireItem()
        val view = viewHolder.itemView
        val position = viewHolder.adapterPosition

        check(isTouchableItem(item)) {
            "A item was swiped that is not a valid target: ${item.javaClass}"
        }

        onSwipeCompleted(item as T, view, position, direction)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)

        if (viewHolder != null) {
            viewHolder as ItemViewHolder

            val item = viewHolder.requireItem()

            check(isTouchableItem(item)) {
                "A item was selected that is not a valid target: ${item.javaClass}"
            }

            (viewHolder.itemView.parent as RecyclerView).hasSelection = true

            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                holderBeingSwiped = viewHolder
                onSwipeStarted(item as T, viewHolder.itemView, viewHolder.adapterPosition)
            } else if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                holderBeingDragged = viewHolder
                onDragStarted(item as T, viewHolder.itemView, viewHolder.adapterPosition)
            }
        } else if (holderBeingDragged != null) {
            onDragReleased(holderBeingDragged!!.item as T, holderBeingDragged!!.itemView)
            holderBeingDragged = null
        } else if (holderBeingSwiped != null) {
            onSwipeReleased(holderBeingSwiped!!.item as T, holderBeingSwiped!!.itemView)
            holderBeingSwiped = null
        }
    }

    private var RecyclerView.hasSelection: Boolean
        get() = getTag(R.id.list_touch_helper_selection_status) == true
        set(value) {
            setTag(R.id.list_touch_helper_selection_status, value)
        }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)

        viewHolder as ItemViewHolder

        clearItemView(viewHolder.item as T, viewHolder.itemView)

        // If multiple touch helpers are in use, one touch helper can pick up buffered touch inputs
        // immediately after another touch event finishes. This leads to things like a view being
        // selected for drag when another view finishes its swipe off animation. To prevent that we
        // keep the recyclerview marked as having an active selection for a brief period after a
        // touch event ends.
        recyclerView.postDelayed(
            { recyclerView.hasSelection = false },
            TOUCH_DEBOUNCE_MILLIS
        )
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        viewHolder as ItemViewHolder

        val item = viewHolder.requireItem()
        check(isTouchableItem(item)) {
            "A item was selected that is not a valid target: ${item.javaClass}"
        }

        val itemView = viewHolder.itemView

        val swipeProgress = if (dX.absoluteValue > dY.absoluteValue) {
            dX / itemView.width
        } else {
            dY / itemView.height
        }

        // Clamp to 1/-1 in the case of side padding where the view can be swiped extra
        val clampedProgress = max(-1f, min(1f, swipeProgress))

        onSwipeProgressChanged(item as T, itemView, clampedProgress, c)
    }

    override fun clearItemView(item: T, itemView: View) {
    }

    override fun onDragStarted(item: T, itemView: View, adapterPosition: Int) {
    }

    override fun onItemMoved(
        fromPosition: Int,
        toPosition: Int,
        itemBeingMoved: T,
        itemView: View
    ) {
    }

    override fun onDragReleased(item: T, itemView: View) {
    }

    override fun onSwipeStarted(item: T, itemView: View, adapterPosition: Int) {
    }

    override fun onSwipeProgressChanged(
        item: T,
        itemView: View,
        swipeProgress: Float,
        canvas: Canvas
    ) {
    }

    override fun onSwipeReleased(item: T, itemView: View) {
    }

    override fun onSwipeCompleted(item: T, itemView: View, position: Int, direction: Int) {
    }

    companion object {
        private const val TOUCH_DEBOUNCE_MILLIS = 300L
    }
}