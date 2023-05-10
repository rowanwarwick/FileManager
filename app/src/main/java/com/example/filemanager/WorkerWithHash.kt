package com.example.filemanager

import android.os.Environment
import com.example.filemanager.DB.DataBase
import com.example.filemanager.DB.Item
import java.io.File
import java.security.MessageDigest

class WorkerWithHash(private val dataBase: DataBase) {
    var allChangedFiles = arrayOf<File>()

    fun hashCodeAllFiles() {
        Thread {
            allChangedFiles =
                allChangedFiles(File(Environment.getExternalStorageDirectory().path)).toTypedArray()
        }.start()
    }

    private fun createHashValue(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(file.readBytes())
        val hexString = StringBuilder()
        for (elem in hash) {
            val hex = Integer.toHexString(0xff and elem.toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
        }
        return hexString.toString()
    }

    private fun allChangedFiles(directory: File): MutableList<File> {
        val listFiles = mutableListOf<File>()
        if (directory.isDirectory) {
            val inDirectory = directory.listFiles() ?: arrayOf<File>()
            for (file in inDirectory) {
                listFiles.addAll(allChangedFiles(file))
            }
        } else {
            val hashFile = createHashValue(directory)
            val hash = dataBase.workWithData().getItem(directory.path)
            if (hash.isEmpty()) {
                dataBase.workWithData()
                    .insertItem(Item(directory.path, hashFile))
            } else if (hash.first().hash != createHashValue(directory)) {
                listFiles.add(directory)
                dataBase.workWithData()
                    .updateItem(Item(directory.path, hashFile))
            }
        }
        return listFiles
    }
}