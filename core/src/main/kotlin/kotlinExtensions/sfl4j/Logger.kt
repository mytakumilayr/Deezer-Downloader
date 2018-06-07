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

package kotlinExtensions.sfl4j

import de.bigboot.deezerdownloader.LoggerHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

//val Any?.LOG: Logger get() {
//    val logger: Logger by lazy {
//        if (this != null) {
//            LoggerFactory.getLogger((this::class as KClass<*>).java)
//        } else {
//            LoggerFactory.getLogger("[NULL]")
//        }
//    }
//    return logger
//}
//
//inline fun Any?.trace(msg: () -> Any?) { if (LOG.isTraceEnabled) LOG.trace(msg.invoke().toString()) }
//inline fun Any?.debug(msg: () -> Any?) { if (LOG.isDebugEnabled) LOG.debug(msg.invoke().toString()) }
//inline fun Any?.info(msg: () -> Any?) { if (LOG.isInfoEnabled) LOG.info(msg.invoke().toString()) }
//inline fun Any?.warn(msg: () -> Any?) { if (LOG.isWarnEnabled) LOG.warn(msg.invoke().toString()) }
//inline fun Any?.error(msg: () -> Any?) { if (LOG.isErrorEnabled) LOG.error(msg.invoke().toString()) }
//inline fun Any?.trace(t: Throwable, msg: () -> Any?) { if (LOG.isTraceEnabled) LOG.trace(msg.invoke().toString(), t) }
//inline fun Any?.debug(t: Throwable, msg: () -> Any?) { if (LOG.isDebugEnabled) LOG.debug(msg.invoke().toString(), t) }
//inline fun Any?.info(t: Throwable, msg: () -> Any?) { if (LOG.isInfoEnabled) LOG.info(msg.invoke().toString(), t) }
//inline fun Any?.warn(t: Throwable, msg: () -> Any?) { if (LOG.isWarnEnabled) LOG.warn(msg.invoke().toString(), t) }
//inline fun Any?.error(t: Throwable, msg: () -> Any?) { if (LOG.isErrorEnabled) LOG.error(msg.invoke().toString(), t) }

fun Any?.trace(msg: () -> Any?) { LoggerHandler.Instance.trace(msg)}
fun Any?.debug(msg: () -> Any?) { LoggerHandler.Instance.debug(msg)}
fun Any?.info(msg: () -> Any?) { LoggerHandler.Instance.info(msg)}
fun Any?.warn(msg: () -> Any?) { LoggerHandler.Instance.warn(msg)}
fun Any?.error(msg: () -> Any?) { LoggerHandler.Instance.error(msg)}
fun Any?.trace(t: Throwable, msg: () -> Any?) { LoggerHandler.Instance.trace(t, msg)}
fun Any?.debug(t: Throwable, msg: () -> Any?) { LoggerHandler.Instance.debug(t, msg)}
fun Any?.info(t: Throwable, msg: () -> Any?) { LoggerHandler.Instance.info(t, msg)}
fun Any?.warn(t: Throwable, msg: () -> Any?) { LoggerHandler.Instance.warn(t, msg)}
fun Any?.error(t: Throwable, msg: () -> Any?) { LoggerHandler.Instance.error(t, msg)}