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

class NoPlatformHandlerException: RuntimeException {
    constructor(handler: Class<out PlatformHandler>, ex: Exception?): super("No PlatformHandler registered for type: ${handler.simpleName}", ex)
    constructor(handler: Class<out PlatformHandler>): super("No PlatformHandler registered for type: ${handler.simpleName}")
}

interface PlatformHandler

open class PlatformHandlerRegister<T: PlatformHandler>(private val clazz: Class<T>) {
    private var _instance: T? = null

    fun register(handler: T) {
        _instance = handler
    }

    val Instance: T get() = _instance
            ?: throw NoPlatformHandlerException(clazz)
}

interface DataHandler: PlatformHandler {
    val dataPath: File
    val applicationPath: File
    val downloadPath: File
    val cacheDir: File

    companion object: PlatformHandlerRegister<DataHandler>(DataHandler::class.java)
}

interface PlatformDownloader {
    interface Listener {
        fun init()
        fun progress(progress: Double)
        fun finished()
        fun error(error: Throwable)
    }
    fun download(trackID: String, url: String, outputFile: File, listener: Listener? = null)
}

interface DownloadHandler: PlatformHandler {
    fun createDownloader(): PlatformDownloader

    companion object: PlatformHandlerRegister<DownloadHandler>(DownloadHandler::class.java)
}

interface LoggerHandler : PlatformHandler {
    fun trace(msg: () -> Any?)
    fun debug(msg: () -> Any?)
    fun info(msg: () -> Any?)
    fun warn(msg: () -> Any?)
    fun error(msg: () -> Any?)
    fun trace(t: Throwable, msg: () -> Any?)
    fun debug(t: Throwable, msg: () -> Any?)
    fun info(t: Throwable, msg: () -> Any?)
    fun warn(t: Throwable, msg: () -> Any?)
    fun error(t: Throwable, msg: () -> Any?)

    companion object: PlatformHandlerRegister<LoggerHandler>(LoggerHandler::class.java)
}

interface ImageHandler : PlatformHandler {
    class Image (
        val binaryData: ByteArray,
        val info: ImageInfo
    )
    class ImageInfo (
        val mimeType: String,
        val width: Int,
        val height: Int
    )
    fun getImageInfo(binaryData: ByteArray): ImageInfo
    fun createImage(data: ByteArray): Image
    fun createScaledImage(data: ByteArray, w: Int = -1, h: Int = -1): Image

    companion object: PlatformHandlerRegister<ImageHandler>(ImageHandler::class.java)
}

interface DialogHandler : PlatformHandler {
    fun askForExistingFileBehaviour(file: File): Config.ExistingFileBehaviour

    companion object: PlatformHandlerRegister<DialogHandler>(DialogHandler::class.java)
}