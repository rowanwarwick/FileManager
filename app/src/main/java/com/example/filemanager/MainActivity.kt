package com.example.filemanager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.filemanager.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity(), FileAdapter.Listener {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (!checkPermission()) requestPermission()
        val files = openFile(Environment.getExternalStorageDirectory().path)
        binding.fileList.layoutManager = LinearLayoutManager(this)
        binding.fileList.adapter = FileAdapter(this, files)
        binding.path.isSelected = true
        binding.path.setOnClickListener {
            if ((it as TextView).text != Environment.getExternalStorageDirectory().path) {
                val path = it.text.dropLastWhile { char -> char != File.separatorChar }.dropLast(1)
                    .toString()
                binding.fileList.adapter = FileAdapter(this, openFile(path))
            }
        }
    }

    fun openFile(path: String): Array<out File> {
        binding.noFiles.visibility = View.INVISIBLE
        binding.path.text = path
        val file = File(path)
        val list = file.listFiles()?.sorted()?.toTypedArray()
        if (list == null || list.isEmpty()) {
            binding.noFiles.visibility = View.VISIBLE
        }
        return list ?: arrayOf()
    }

    fun checkPermission() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    fun requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            Toast.makeText(this, "please activate permission in settings", Toast.LENGTH_SHORT)
                .show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        }
    }

    override fun onClick(file: File) {
        if (file.isDirectory) {
            val files = openFile(file.path)
            binding.fileList.adapter = FileAdapter(this, files)
        } else {
            val imagePath = file.path
            val fileUri = FileProvider.getUriForFile(
                this,
                this.applicationContext.packageName + ".provider",
                File(imagePath)
            )
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(
                fileUri, when {
                    fileUri.toString().contains(".doc", true) -> "application/msword"
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
            startActivity(intent)
        }
    }

}

