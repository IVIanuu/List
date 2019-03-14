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
import com.ivianuu.stdlibx.firstNotNullResultOrNull
import com.ivianuu.stdlibx.safeAs

/**
 * View holder used in [ModelAdapter]s
 */
class ModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    /**
     * The current bound model if any
     */
    var model: ListModel<ModelHolder>? = null
        private set

    /**
     * The underlying model holder
     */
    lateinit var holder: ModelHolder
        private set
    private var holderCreated = false

    internal fun bind(model: ListModel<*>, payloads: List<Any?>) {
        this.model = model as ListModel<ModelHolder>

        if (!holderCreated) {
            holderCreated = true
            holder = model.createHolder()
            holder.bindView(itemView)
        }

        model.bind(holder, payloads.firstNotNullResultOrNull { it.safeAs<ListModel<*>>() })
    }

    internal fun unbind() {
        model?.unbind(holder)
        model = null
    }

}

/**
 * Returns the currently bound model or throws
 */
fun ModelViewHolder.requireModel(): ListModel<ModelHolder> = model ?: error("model is null")