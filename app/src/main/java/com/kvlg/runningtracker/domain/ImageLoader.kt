package com.kvlg.runningtracker.domain

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Konstantin Koval
 * @since 20.09.2020
 */
class ImageLoader(
    private val context: Context
) {
    suspend fun saveImageIntoDisk(bmp: Bitmap): String? = withContext(Dispatchers.IO) {
        val file = getOutputFile()
        if (file == null) {
            Timber.tag(TAG).d("Error creating media file, check storage permissions: ")
            return@withContext null
        }
        try {
            val fos = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
            file.absolutePath
        } catch (e: FileNotFoundException) {
            Timber.tag(TAG).d(e)
            null
        } catch (e: IOException) {
            Timber.tag(TAG).d(e)
            null
        }
    }

    private fun getOutputFile(): File? {
        val dir = File(context.filesDir.absolutePath + "/runsImages")
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                return null
            }
        }
        val timeStamp: String = SimpleDateFormat("ddMMyyyy_HHmm", Locale.getDefault()).format(Date())
        val fileName = "Run_$timeStamp.png"
        return File(dir.absolutePath + File.separator + fileName)
    }

    companion object {
        private const val TAG = "ImageLoader"
    }
}