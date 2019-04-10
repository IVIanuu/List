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

import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView.Adapter

internal class DiffResult private constructor(
    val previousItems: List<Item<*>>,
    val newItems: List<Item<*>>,
    val differResult: DiffUtil.DiffResult?
) {

    fun dispatchTo(adapter: Adapter<*>) {
        dispatchTo(AdapterListUpdateCallback(adapter))
    }

    fun dispatchTo(callback: ListUpdateCallback) {
        if (differResult != null) {
            differResult.dispatchUpdatesTo(callback)
        } else if (newItems.isEmpty() && !previousItems.isEmpty()) {
            callback.onRemoved(0, previousItems.size)
        } else if (!newItems.isEmpty() && previousItems.isEmpty()) {
            callback.onInserted(0, newItems.size)
        }
    }

    companion object {

        fun noop(items: List<Item<*>>): DiffResult {
            return DiffResult(items, items, null)
        }

        fun inserted(newItems: List<Item<*>>): DiffResult {
            return DiffResult(emptyList(), newItems, null)
        }

        fun cleared(previousItems: List<Item<*>>): DiffResult {
            return DiffResult(previousItems, emptyList(), null)
        }

        fun diff(
            previousItems: List<Item<*>>,
            newItems: List<Item<*>>,
            differResult: DiffUtil.DiffResult
        ): DiffResult {
            return DiffResult(previousItems, newItems, differResult)
        }
    }
}