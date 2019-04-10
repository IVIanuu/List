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
import androidx.recyclerview.widget.RecyclerView

/**
 * View holder used in [ItemAdapter]s
 */
class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    /**
     * The current bound item if any
     */
    var item: Item<Holder>? = null
        private set

    /**
     * The underlying item holder
     */
    lateinit var holder: Holder
        private set

    internal fun bind(item: Item<*>) {
        this.item = item as Item<Holder>

        if (!this::holder.isInitialized) {
            holder = item.newHolder()
            holder.setView(itemView)
        }

        item.bindHolder(holder)
    }

    internal fun unbind() {
        item?.unbindHolder(holder)
        item = null
    }

}

/**
 * Returns the currently bound item or throws
 */
fun ItemViewHolder.requireItem(): Item<Holder> = item ?: error("item is null")