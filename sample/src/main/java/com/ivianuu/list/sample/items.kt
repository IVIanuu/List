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

package com.ivianuu.list.sample

import com.ivianuu.list.ItemController
import kotlinx.android.synthetic.main.item_button.button
import kotlinx.android.synthetic.main.item_count.count_text
import kotlinx.android.synthetic.main.item_count.dec_button
import kotlinx.android.synthetic.main.item_count.inc_button
import kotlinx.android.synthetic.main.item_simple.title

fun ItemController.ButtonItem(text: String, onClick: () -> Unit) =
    item(id = text, layoutRes = R.layout.item_button) {
        button.text = text
        button.setOnClickListener { onClick() }
    }

fun ItemController.CountItem(count: Int, onIncClick: () -> Unit, onDecClick: () -> Unit) = item(
    id = "count",
    layoutRes = R.layout.item_count,
    properties = listOf(count)
) {
    count_text.text = "Count is $count"
    inc_button.setOnClickListener { onIncClick() }
    dec_button.setOnClickListener { onDecClick() }
}

fun ItemController.SimpleItem(text: String, onClick: () -> Unit) = item {
    id(text)
    layoutRes(R.layout.item_simple)
    bind {
        title.text = text
        containerView.setOnClickListener { onClick() }
    }
}
