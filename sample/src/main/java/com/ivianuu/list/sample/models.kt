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

import com.ivianuu.list.ListModelFactory
import com.ivianuu.list.common.LayoutContainerHolder
import com.ivianuu.list.common.LayoutContainerModel
import kotlinx.android.synthetic.main.item_button.button
import kotlinx.android.synthetic.main.item_count.count
import kotlinx.android.synthetic.main.item_simple.title

class ButtonModel : LayoutContainerModel(layoutRes = R.layout.item_button) {
    var buttonText by requiredProperty<String>("buttonText")

    override fun bind(holder: LayoutContainerHolder) {
        super.bind(holder)
        holder.button.text = buttonText
    }

    companion object : ListModelFactory<ButtonModel>(::ButtonModel)
}

class CountModel : LayoutContainerModel(id = "count", layoutRes = R.layout.item_count) {
    var count by requiredProperty<Int>("count")

    override fun bind(holder: LayoutContainerHolder) {
        super.bind(holder)
        holder.count.text = "Count is $count"
    }

    companion object : ListModelFactory<CountModel>(::CountModel)
}

class SimpleModel : LayoutContainerModel(layoutRes = R.layout.item_simple) {
    var text by requiredProperty<String>("text")

    override fun bind(holder: LayoutContainerHolder) {
        super.bind(holder)
        holder.title.text = text
    }

    companion object : ListModelFactory<SimpleModel>(::SimpleModel)
}
