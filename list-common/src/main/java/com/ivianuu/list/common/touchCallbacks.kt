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
import com.ivianuu.list.Item

interface BaseItemTouchCallback<T : Item<*>> {

    fun getMovementFlagsForItem(item: T, adapterPosition: Int): Int

    fun clearItemView(item: T, itemView: View)

}

interface ItemDragCallback<T : Item<*>> : BaseItemTouchCallback<T> {

    fun onDragStarted(item: T, itemView: View, adapterPosition: Int)

    fun onItemMoved(fromPosition: Int, toPosition: Int, itemBeingMoved: T, itemView: View)

    fun onDragReleased(item: T, itemView: View)

}

interface ItemSwipeCallback<T : Item<*>> : BaseItemTouchCallback<T> {

    fun onSwipeStarted(item: T, itemView: View, adapterPosition: Int)

    fun onSwipeProgressChanged(
        item: T,
        itemView: View,
        swipeProgress: Float,
        canvas: Canvas
    )

    fun onSwipeReleased(item: T, itemView: View)

    fun onSwipeCompleted(item: T, itemView: View, position: Int, direction: Int)
}

