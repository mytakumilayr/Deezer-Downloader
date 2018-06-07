/*
 * Copyright 2017 BigBoot
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.bigboot.deezerdownloader

import java.io.File
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger
import kotlin.properties.Delegates

class Download(val entry: Entry, val id: Int = Download.nextID.andIncrement) {
    val listeners = ArrayList<(Download)->Unit>()

    var progress by Delegates.observable(0.0) {_, _, _ -> listeners.forEach { it(this) }}
    var count by Delegates.observable(0) {_, _, _ -> listeners.forEach { it(this) }}
    var countFinished by Delegates.observable(0) {_, _, _ -> listeners.forEach { it(this) }}
    var state by Delegates.observable(State.Waiting) {_, _, _ -> listeners.forEach { it(this) }}
    var filenameTemplate by Delegates.observable("%track%") {_, _, _ -> listeners.forEach { it(this) }}

    fun onUpdate(cb: (Download)->Unit): (Download)->Unit {
        listeners.add(cb)
        return cb
    }

    fun removeCallback(cb: (Download)->Unit) {
        listeners.remove(cb)
    }

    enum class State(val priority: Int) {
        Preprocessing(2), Running(4), Waiting(1), Error(6), Finished(0), Skipped(0), Preprocessed(3), Tagging(5)
    }
    var containingListSize: Int = 0

    var handle: Future<*>? = null
    var cancelled: Boolean = false; private set

    fun cancel() {
        cancelled = true
    }

    var outputFile: File? = null
    val children = ArrayList<Download>()

    var preprocessed = false

    companion object {
        private val nextID = AtomicInteger(1000)
    }
}