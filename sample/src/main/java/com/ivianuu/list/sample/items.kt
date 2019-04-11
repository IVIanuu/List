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

import com.ivianuu.list.ItemFactory
import com.ivianuu.list.common.KotlinHolder
import com.ivianuu.list.common.KotlinItem
import kotlinx.android.synthetic.main.item_button.button
import kotlinx.android.synthetic.main.item_count.count
import kotlinx.android.synthetic.main.item_simple.title

class ButtonItem : KotlinItem(layoutRes = R.layout.item_button) {
    var buttonText by idProperty<String>("buttonText")

    override fun bind(holder: KotlinHolder) {
        super.bind(holder)
        holder.button.text = buttonText
    }

    companion object : ItemFactory<ButtonItem>(::ButtonItem)
}

class CountItem : KotlinItem(id = "count", layoutRes = R.layout.item_count) {
    var count by requiredProperty<Int>("count")

    override fun bind(holder: KotlinHolder) {
        super.bind(holder)
        holder.count.text = "Count is $count"
    }

    companion object : ItemFactory<CountItem>(::CountItem)
}

class SimpleItem : KotlinItem(layoutRes = R.layout.item_simple) {
    var text by idProperty<String>("text")

    override fun bind(holder: KotlinHolder) {
        super.bind(holder)
        holder.title.text = text
    }

    companion object : ItemFactory<SimpleItem>(::SimpleItem)
}
