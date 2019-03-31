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

import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper

internal val mainThreadHandler: Handler by lazy { createHandler(Looper.getMainLooper()) }

internal val backgroundHandler: Handler by lazy {
    val handlerThread = HandlerThread("list:bg")
    handlerThread.start()
    createHandler(handlerThread.looper)
}

private fun createHandler(looper: Looper): Handler {
    // Standard way of exposing async handler on older api's from the support library
    // https://android.googlesource.com/platform/frameworks/support/+/androidx-master-dev/core
    // /src/main/java/androidx/core/os/HandlerCompat.java#51
    return if (Build.VERSION.SDK_INT >= 28) {
        Handler.createAsync(looper)
    } else {
        Handler::class.java.getDeclaredConstructor(
            Looper::class.java,
            Handler.Callback::class.java,
            Boolean::class.javaPrimitiveType
        ).newInstance(looper, null, true) as Handler
    }
}