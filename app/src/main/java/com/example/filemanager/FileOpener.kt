package com.example.filemanager

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

class FileOpener(private val context: Context, val path: String) {
    fun opener() {
        val fileUri = FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".provider",
            File(path)
        )
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(
            fileUri, when {
                fileUri.toString().contains(".doc", true) || fileUri.toString()
                    .contains(".txt", true) -> "application/msword"

                fileUri.toString().contains(".pdf", true) -> "application/pdf"
                fileUri.toString().contains(".mp3", true) || fileUri.toString()
                    .contains(".wav", true) -> "audio/*"

                fileUri.toString().contains(".jpeg", true) || fileUri.toString()
                    .contains(".png", true) || fileUri.toString()
                    .contains(".jpg", true) -> "image/*"

                fileUri.toString().contains(".mp4", true) -> "video/*"
                else -> "*/*"
            }
        )
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)
    }
}