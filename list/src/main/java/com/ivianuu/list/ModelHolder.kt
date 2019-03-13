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

/**
 * View holder for [ListModel]s
 */
abstract class ModelHolder {

    /**
     * The view of this holder
     */
    lateinit var view: View

    /**
     * Will be called when the view was created
     * This function can be used to bind click listeners etc.
     */
    protected open fun onBindView(view: View) {
    }

    internal fun bindView(view: View) {
        this.view = view
        onBindView(view)
    }

}