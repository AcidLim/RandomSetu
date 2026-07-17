package com.acid.setu.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

object FileUtils {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    /**
     * 从URL下载图片到缓存目录
     */
    suspend fun downloadImageToCache(context: Context, url: String): File? {
        return try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) return null

            val inputStream = response.body?.byteStream() ?: return null

            // 提取文件扩展名
            val ext = extractExtension(url)
            val fileName = "setu_${System.currentTimeMillis()}$ext"

            // 保存到缓存目录
            val cacheFile = File(context.cacheDir, fileName)

            FileOutputStream(cacheFile).use { output ->
                inputStream.use { input ->
                    input.copyTo(output)
                }
            }

            cacheFile
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 保存图片到Download文件夹（兼容Android 10+）
     */
    suspend fun saveToDownload(context: Context, sourceFile: File): Boolean {
        return try {
            val fileName = sourceFile.name
            val mimeType = getMimeType(fileName)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ 使用MediaStore
                saveViaMediaStore(context, sourceFile, fileName, mimeType)
            } else {
                // Android 9及以下使用文件操作
                saveViaFileSystem(context, sourceFile, fileName)
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun saveViaMediaStore(
        context: Context,
        sourceFile: File,
        fileName: String,
        mimeType: String
    ): Boolean {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            // 对于 Images 集合，只能使用 DCIM 或 Pictures
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        ) ?: return false

        val outputStream = context.contentResolver.openOutputStream(uri) ?: return false

        return try {
            outputStream.use { output ->
                sourceFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun saveViaFileSystem(
        context: Context,
        sourceFile: File,
        fileName: String
    ): Boolean {
        val downloadDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        ) ?: return false

        if (!downloadDir.exists() && !downloadDir.mkdirs()) return false

        val destFile = File(downloadDir, fileName)
        sourceFile.copyTo(destFile, overwrite = true)
        return true
    }

    /**
     * 从URL提取文件扩展名
     */
    private fun extractExtension(url: String): String {
        val lastDot = url.lastIndexOf(".")
        if (lastDot == -1) return ".jpg"

        val ext = url.substringAfterLast(".")
        return if (ext.length in 2..5) ".$ext" else ".jpg"
    }

    /**
     * 根据文件名获取MIME类型
     */
    private fun getMimeType(fileName: String): String {
        return when (fileName.substringAfterLast(".").lowercase()) {
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            else -> "image/jpeg"
        }
    }

    /**
     * 删除缓存目录中的所有图片
     */
    fun clearAllCache(context: Context) {
        context.cacheDir.listFiles()?.filter {
            it.name.startsWith("setu_")
        }?.forEach {
            it.delete()
        }
    }
}