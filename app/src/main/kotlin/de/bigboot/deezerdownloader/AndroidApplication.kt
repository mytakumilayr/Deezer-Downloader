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

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.support.multidex.MultiDexApplication
import android.util.Log
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadListener
import com.liulishuo.filedownloader.FileDownloader
import com.liulishuo.filedownloader.services.DownloadMgrInitialParams
import kotlinExtensions.java.I18n
import nl.komponents.kovenant.android.startKovenant
import nl.komponents.kovenant.android.stopKovenant
import java.io.ByteArrayOutputStream
import java.io.File

class AndroidPlatformHandler(private val application: Application): DownloadHandler, DataHandler, LoggerHandler, ImageHandler {
    override fun trace(msg: () -> Any?) {
        Log.v("DEEZER_DOWNLOADER", msg.invoke()?.toString())
    }

    override fun debug(msg: () -> Any?) {
        Log.d("DEEZER_DOWNLOADER", msg.invoke()?.toString())
    }

    override fun info(msg: () -> Any?) {
        Log.i("DEEZER_DOWNLOADER", msg.invoke()?.toString())
    }

    override fun warn(msg: () -> Any?) {
        Log.w("DEEZER_DOWNLOADER", msg.invoke()?.toString())
    }

    override fun error(msg: () -> Any?) {
        Log.e("DEEZER_DOWNLOADER", msg.invoke()?.toString())
    }

    override fun trace(t: Throwable, msg: () -> Any?) {
        Log.v("DEEZER_DOWNLOADER", msg.invoke()?.toString(), t)
    }

    override fun debug(t: Throwable, msg: () -> Any?) {
        Log.d("DEEZER_DOWNLOADER", msg.invoke()?.toString(), t)
    }

    override fun info(t: Throwable, msg: () -> Any?) {
        Log.i("DEEZER_DOWNLOADER", msg.invoke()?.toString(), t)
    }

    override fun warn(t: Throwable, msg: () -> Any?) {
        Log.w("DEEZER_DOWNLOADER", msg.invoke()?.toString(), t)
    }

    override fun error(t: Throwable, msg: () -> Any?) {
        Log.e("DEEZER_DOWNLOADER", msg.invoke()?.toString(), t)
    }

    override fun createDownloader(): PlatformDownloader = object: PlatformDownloader {
        override fun download(trackID: String, url: String, outputFile: File, listener: PlatformDownloader.Listener?) {

            FileDownloader.getImpl().create("$trackID;$url")
                    .setPath(outputFile.canonicalPath)
                    .setListener(object : FileDownloadListener() {
                        override fun warn(task: BaseDownloadTask?) {}
                        override fun paused(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {}
                        override fun pending(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {}


                        override fun completed(task: BaseDownloadTask?) {
                            listener?.finished()
                        }

                        override fun error(task: BaseDownloadTask?, e: Throwable) {
                            listener?.error(e)
                        }

                        override fun progress(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                            listener?.progress(soFarBytes.toDouble() / totalBytes.toDouble())
                        }

                    }).start()
            listener?.init()
        }
    }

    override fun getImageInfo(binaryData: ByteArray): ImageHandler.ImageInfo {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        var bitmap = BitmapFactory.decodeByteArray(binaryData, 0, binaryData.size, options)
        return ImageHandler.ImageInfo(options.outMimeType, options.outWidth, options.outHeight)
    }


    override fun createScaledImage(data: ByteArray, w: Int, h: Int): ImageHandler.Image {
        if (w <= 0 && h <= 0)
            throw IllegalArgumentException("width and height <= 0")

        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)

        val scaled = Bitmap.createScaledBitmap(bitmap, w, h, true)

        val imageInfo = ImageHandler.ImageInfo("image/png", w, h)

        val bitmapData = ByteArrayOutputStream(1024*1024)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bitmapData)

        return ImageHandler.Image(bitmapData.toByteArray(), imageInfo)
    }

    override fun createImage(data: ByteArray): ImageHandler.Image {

        val options = BitmapFactory.Options()
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size, options)

        val imageInfo = ImageHandler.ImageInfo("image/png", options.outWidth, options.outHeight)

        val bitmapData = ByteArrayOutputStream(1024*1024)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bitmapData)

        return ImageHandler.Image(bitmapData.toByteArray(), imageInfo)
    }

    override val dataPath: File
        get() = application.filesDir
    override val applicationPath: File
        get() = application.filesDir
    override val downloadPath: File
        get() = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    override val cacheDir: File
        get() = application.cacheDir
}


class AndroidApplication: MultiDexApplication() {
    init {
        val platformhandler = AndroidPlatformHandler(this)

        DataHandler.register(platformhandler)
        DownloadHandler.register(platformhandler)
        LoggerHandler.register(platformhandler)
        ImageHandler.register(platformhandler)
    }

    override fun onCreate() {
        I18n.defaultBundle = Utf8ResourceBundle.getBundle("strings")

        startKovenant()
        FileDownloader.init(applicationContext, DownloadMgrInitialParams.InitCustomMaker()
                .connectionCreator(ConnectionCreator()))
    }

    override fun onTerminate() {
        super.onTerminate()
        stopKovenant()
    }
}