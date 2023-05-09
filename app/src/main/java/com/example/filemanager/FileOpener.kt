package com.example.filemanager

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
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
        try {
            intent.setDataAndType(fileUri, detectFormat())
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "I can't find need app", Toast.LENGTH_SHORT).show()
        }
    }

    private fun detectFormat(): String {
        return when (path.substringAfterLast(".", "")) {
            "txt" -> "text/plain"
            "doc" -> "application/msword"
            "pdf" -> "application/pdf"
            "mp3", "wav" -> "audio/*"
            "jpeg", "png", "jpg" -> "image/*"
            "mp4" -> "video/*"
            else -> "*/*"
        }
    }
}