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

package kotlinExtensions.java

import java.util.Comparator
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicLong


class PriorityFuture<T>(private val src: RunnableFuture<T>, val priority: Int) : RunnableFuture<T> {
    val id = nextID.andIncrement

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        return src.cancel(mayInterruptIfRunning)
    }

    override fun isCancelled(): Boolean {
        return src.isCancelled
    }

    override fun isDone(): Boolean {
        return src.isDone
    }

    override fun get(): T {
        return src.get()
    }

    override fun get(timeout: Long, unit: TimeUnit): T {
        return src.get(timeout, unit)
    }

    override fun run() {
        src.run()
    }

    companion object {
        val nextID = AtomicLong(0L)
    }
}

interface PriorityCallable<V>: Callable<V> {
    val priority: Int
}

interface PriorityRunnable: Runnable {
    val priority: Int
}

internal class PriorityCallableImpl<V>(override val priority: Int, val task: ()->V): PriorityCallable<V> {
    override fun call(): V = task()
}

internal class PriorityRunnableImpl(override val priority: Int, val task: ()->Unit): PriorityRunnable {
    override fun run() = task()
}

class PriorityExecutor(nThreads: Int, val defaultPriority: Int = 0, comparator: Comparator<Runnable> = LOW_BEFORE_HIGH):
        ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, PriorityBlockingQueue<Runnable>(10, comparator)) {

    override fun <T : Any?> newTaskFor(callable: Callable<T>?): RunnableFuture<T> {
        return super.newTaskFor(callable).let {
            when (callable) {
                is PriorityCallable -> PriorityFuture<T>(it, callable.priority)
                else -> PriorityFuture<T>(it, defaultPriority)
            }
        }
    }

    override fun <T : Any?> newTaskFor(runnable: Runnable?, value: T): RunnableFuture<T> {
        return super.newTaskFor(runnable, value).let {
            when (runnable) {
                is PriorityRunnable -> PriorityFuture<T>(it, runnable.priority)
                else -> PriorityFuture<T>(it, defaultPriority)
            }
        }
    }

    fun <T : Any?> submit(priority: Int, task: ()->T): Future<T> {
        return super.submit(PriorityCallableImpl(priority, task))
    }

    fun execute(priority: Int, task: ()->Unit) {
        super.execute(PriorityRunnableImpl(priority, task))
    }

    companion object {
        val LOW_BEFORE_HIGH: Comparator<Runnable> = Comparator { o1, o2 ->
            if (o1 == null && o2 == null)
                0
            else if (o1 == null)
                -1
            else if (o2 == null)
                1
            else {
                val p1 = (o1 as PriorityFuture<*>)
                val p2 = (o2 as PriorityFuture<*>)

                if (p1.priority > p2.priority)
                    1
                else if (p1.priority == p2.priority)
                    (p1.id - p2.id).toInt()
                else
                    -1
            }
        }
        val HIGH_BEFORE_HIGH: Comparator<Runnable> = Comparator { o1, o2 ->
            if (o1 == null && o2 == null)
                0
            else if (o1 == null)
                -1
            else if (o2 == null)
                1
            else {
                val p1 = (o1 as PriorityFuture<*>)
                val p2 = (o2 as PriorityFuture<*>)

                if (p1.priority > p2.priority)
                    -1
                else if (p1.priority == p2.priority)
                    (p1.id - p2.id).toInt()
                else
                    1
            }
        }
    }
}