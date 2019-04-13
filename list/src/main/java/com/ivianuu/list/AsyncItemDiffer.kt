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

import androidx.recyclerview.widget.DiffUtil
import java.util.*

internal class AsyncItemDiffer(private val resultCallback: (DiffResult) -> Unit) {

    private val generationTracker = GenerationTracker()

    @Volatile private var list: List<Item<*>> = emptyList()

    var currentList = emptyList<Item<*>>()
        private set

    val isDiffInProgress: Boolean
        get() = generationTracker.hasUnfinishedGeneration()

    fun cancelDiff(): Boolean {
        return generationTracker.finishMaxGeneration()
    }

    fun forceListOverride(newList: List<Item<*>>): Boolean {
        // We need to make sure that generation changes and list updates are synchronized
        val interruptedDiff = cancelDiff()
        val generation = generationTracker.incrementAndGetNextScheduled()
        tryLatchList(newList, generation)
        return interruptedDiff
    }

    fun submitList(newList: List<Item<*>>) {
        val runGeneration: Int
        val previousList: List<Item<*>>

        println("diff: submit list $newList")

        synchronized(this) {
            // Incrementing generation means any currently-running diffs are discarded when they finish
            // We synchronize to guarantee list object and generation number are in sync
            runGeneration = generationTracker.incrementAndGetNextScheduled()
            previousList = list
        }

        if (newList == previousList) {
            println("diff: noop")
            // nothing to do
            onRunCompleted(runGeneration, newList, DiffResult.noop(previousList))
            return
        }

        if (newList.isEmpty()) {
            println("diff: cleared")
            // fast simple clear all
            var result: DiffResult? = null
            if (!previousList.isEmpty()) {
                result = DiffResult.cleared(previousList)
            }
            onRunCompleted(runGeneration, emptyList(), result)
            return
        }

        if (previousList.isEmpty()) {
            println("diff: insert from empty")
            // fast simple first insert
            onRunCompleted(runGeneration, newList, DiffResult.inserted(newList))
            return
        }

        val callback = DiffCallback(previousList, newList)

        backgroundThread {
            println("diff: perform diff")
            val result = DiffUtil.calculateDiff(callback)
            onRunCompleted(runGeneration, newList, DiffResult.diff(previousList, newList, result))
        }
    }

    private fun onRunCompleted(
        runGeneration: Int,
        newList: List<Item<*>>,
        result: DiffResult?
    ) {
        // We use an asynchronous handler so that the Runnable can be posted directly back to the main
        // thread without waiting on view invalidation synchronization.
        mainThread {
            val dispatchResult = tryLatchList(newList, runGeneration)
            if (result != null && dispatchResult) {
                resultCallback(result)
            }
        }
    }

    private fun tryLatchList(
        newList: List<Item<*>>,
        runGeneration: Int
    ): Boolean {
        if (generationTracker.finishGeneration(runGeneration)) {
            list = newList
            currentList = Collections.unmodifiableList(newList)
            return true
        }

        return false
    }

    private class GenerationTracker {

        // Max generation of currently scheduled runnable
        @Volatile private var maxScheduledGeneration: Int = 0
        @Volatile private var maxFinishedGeneration: Int = 0

        internal fun incrementAndGetNextScheduled(): Int = synchronized(this) {
            ++maxScheduledGeneration
        }

        internal fun finishMaxGeneration(): Boolean = synchronized(this) {
            val isInterrupting = hasUnfinishedGeneration()
            maxFinishedGeneration = maxScheduledGeneration
            return@finishMaxGeneration isInterrupting
        }

        internal fun hasUnfinishedGeneration(): Boolean = synchronized(this) {
            maxScheduledGeneration > maxFinishedGeneration
        }

        internal fun finishGeneration(runGeneration: Int): Boolean = synchronized(this) {
            val isLatestGeneration =
                maxScheduledGeneration == runGeneration && runGeneration > maxFinishedGeneration

            if (isLatestGeneration) {
                maxFinishedGeneration = runGeneration
            }

            return@synchronized isLatestGeneration
        }
    }

    private class DiffCallback(
        val oldList: List<Item<*>>,
        val newList: List<Item<*>>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition].id == newList[newItemPosition].id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition] == newList[newItemPosition]

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? =
            oldList[oldItemPosition]
    }
}