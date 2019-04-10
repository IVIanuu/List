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
import androidx.recyclerview.widget.ItemTouchHelper.Callback.makeMovementFlags
import androidx.recyclerview.widget.RecyclerView
import com.ivianuu.list.Item
import java.util.*
import kotlin.reflect.KClass

object ItemTouchHelper {

    fun dragging(recyclerView: RecyclerView): DragBuilder {
        return DragBuilder(recyclerView)
    }

    class DragBuilder internal constructor(private val recyclerView: RecyclerView) {
        fun vertical(): DragBuilder2 {
            return directions(ItemTouchHelper.UP or ItemTouchHelper.DOWN)
        }

        fun horizontal(): DragBuilder2 {
            return directions(ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
        }

        fun grid(): DragBuilder2 {
            return directions(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT
                        or ItemTouchHelper.RIGHT
            )
        }

        fun directions(directionFlags: Int): DragBuilder2 {
            return DragBuilder2(
                recyclerView,
                makeMovementFlags(directionFlags, 0)
            )
        }
    }

    class DragBuilder2 internal constructor(
        private val recyclerView: RecyclerView,
        private val movementFlags: Int
    ) {

        fun <U : Item<*>> target(targetItemClass: KClass<U>): DragBuilder3<U> {
            val targetClasses = ArrayList<KClass<out Item<*>>>(1)
            targetClasses.add(targetItemClass)

            return DragBuilder3(
                recyclerView, movementFlags, targetItemClass,
                targetClasses
            )
        }

        fun targets(vararg targetItemClasses: KClass<out Item<*>>): DragBuilder3<Item<*>> {
            return DragBuilder3(
                recyclerView, movementFlags, Item::class,
                targetItemClasses.toList()
            )
        }

        fun all(): DragBuilder3<Item<*>> {
            return target(Item::class)
        }
    }

    class DragBuilder3<U : Item<*>> internal constructor(
        private val recyclerView: RecyclerView,
        private val movementFlags: Int,
        private val targetItemClass: KClass<U>,
        private val targetItemClasses: List<KClass<out Item<*>>>
    ) {

        fun callbacks(callbacks: DragCallbacks<U>): ItemTouchHelper {
            val itemTouchHelper =
                ItemTouchHelper(object : ItemTouchCallback<U>(targetItemClass) {

                    override fun getMovementFlagsForItem(item: U, adapterPosition: Int): Int {
                        return movementFlags
                    }

                    override fun isTouchableItem(item: Item<*>): Boolean {
                        val isTargetType = if (targetItemClasses.size == 1) {
                            super.isTouchableItem(item)
                        } else {
                            targetItemClasses.contains(item::class)
                        }

                        return isTargetType && callbacks.isDragEnabledForItem(item as U)
                    }

                    override fun onDragStarted(item: U, itemView: View, adapterPosition: Int) {
                        callbacks.onDragStarted(item, itemView, adapterPosition)
                    }

                    override fun onDragReleased(item: U, itemView: View) {
                        callbacks.onDragReleased(item, itemView)
                    }

                    override fun onItemMoved(
                        fromPosition: Int,
                        toPosition: Int,
                        itemBeingMoved: U,
                        itemView: View
                    ) {
                        callbacks.onItemMoved(fromPosition, toPosition, itemBeingMoved, itemView)
                    }

                    override fun clearItemView(item: U, itemView: View) {
                        callbacks.clearItemView(item, itemView)
                    }

                })

            itemTouchHelper.attachToRecyclerView(recyclerView)

            return itemTouchHelper
        }
    }

    abstract class DragCallbacks<T : Item<*>> :
        ItemDragCallback<T> {
        fun isDragEnabledForItem(item: T): Boolean = true
        override fun getMovementFlagsForItem(item: T, adapterPosition: Int): Int = 0

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

        override fun clearItemView(item: T, itemView: View) {
        }
    }

    fun swiping(recyclerView: RecyclerView): SwipeBuilder {
        return SwipeBuilder(recyclerView)
    }

    class SwipeBuilder internal constructor(private val recyclerView: RecyclerView) {

        fun right(): SwipeBuilder2 {
            return directions(ItemTouchHelper.RIGHT)
        }

        fun left(): SwipeBuilder2 {
            return directions(ItemTouchHelper.LEFT)
        }

        fun leftAndRight(): SwipeBuilder2 {
            return directions(ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
        }

        fun directions(directionFlags: Int): SwipeBuilder2 {
            return SwipeBuilder2(
                recyclerView,
                makeMovementFlags(0, directionFlags)
            )
        }
    }

    class SwipeBuilder2 internal constructor(
        private val recyclerView: RecyclerView,
        private val movementFlags: Int
    ) {

        fun <U : Item<*>> target(targetItemClass: KClass<U>): SwipeBuilder3<U> {
            val targetClasses = ArrayList<KClass<out Item<*>>>(1)
            targetClasses.add(targetItemClass)

            return SwipeBuilder3(
                recyclerView, movementFlags, targetItemClass,
                targetClasses
            )
        }

        fun targets(
            vararg targetItemClasses: KClass<out Item<*>>
        ): SwipeBuilder3<Item<*>> {
            return SwipeBuilder3(
                recyclerView, movementFlags, Item::class,
                targetItemClasses.toList()
            )
        }

        fun all(): SwipeBuilder3<Item<*>> {
            return target(Item::class)
        }
    }

    class SwipeBuilder3<U : Item<*>> internal constructor(
        private val recyclerView: RecyclerView,
        private val movementFlags: Int,
        private val targetItemClass: KClass<U>,
        private val targetItemClasses: List<KClass<out Item<*>>>
    ) {

        fun callbacks(callbacks: SwipeCallbacks<U>): ItemTouchHelper {
            val itemTouchHelper =
                ItemTouchHelper(object : ItemTouchCallback<U>(targetItemClass) {

                    override fun getMovementFlagsForItem(item: U, adapterPosition: Int): Int {
                        return movementFlags
                    }

                    override fun isTouchableItem(item: Item<*>): Boolean {
                        val isTargetType = if (targetItemClasses.size == 1)
                            super.isTouchableItem(item)
                        else
                            targetItemClasses.contains(item::class)


                        return isTargetType && callbacks.isSwipeEnabledForItem(item as U)
                    }

                    override fun onSwipeStarted(item: U, itemView: View, adapterPosition: Int) {
                        callbacks.onSwipeStarted(item, itemView, adapterPosition)
                    }

                    override fun onSwipeProgressChanged(
                        item: U, itemView: View, swipeProgress: Float,
                        canvas: Canvas
                    ) {
                        callbacks.onSwipeProgressChanged(item, itemView, swipeProgress, canvas)
                    }

                    override fun onSwipeCompleted(
                        item: U,
                        itemView: View,
                        position: Int,
                        direction: Int
                    ) {
                        callbacks.onSwipeCompleted(item, itemView, position, direction)
                    }

                    override fun onSwipeReleased(item: U, itemView: View) {
                        callbacks.onSwipeReleased(item, itemView)
                    }

                    override fun clearItemView(item: U, itemView: View) {
                        callbacks.clearItemView(item, itemView)
                    }

                })

            itemTouchHelper.attachToRecyclerView(recyclerView)

            return itemTouchHelper
        }
    }

    abstract class SwipeCallbacks<T : Item<*>> :
        ItemSwipeCallback<T> {
        fun isSwipeEnabledForItem(item: T): Boolean = true
        override fun getMovementFlagsForItem(item: T, adapterPosition: Int): Int = 0

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

        override fun clearItemView(item: T, itemView: View) {
        }
    }
}