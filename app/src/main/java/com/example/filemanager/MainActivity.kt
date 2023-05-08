package com.example.filemanager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import com.example.filemanager.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes

class MainActivity : AppCompatActivity(), FileAdapter.Listener {
    private lateinit var adapterFiles: FileAdapter
    private lateinit var binding: ActivityMainBinding
    private var pathToDirectory = Environment.getExternalStorageDirectory().path

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        if (!checkPermission()) requestPermission()
        binding.path.isSelected = true
        adapterFiles = FileAdapter(this, File(pathToDirectory).listFiles() ?: arrayOf())
        binding.fileList.adapter = adapterFiles
        watchFilesInRecycleView(pathToDirectory)
        openNavigationView()
        goInUpFolder()
        chooseKindSorting()
    }

    private fun sortingArrayFiles(files: Array<File>) {
        val menu = binding.menu.menu
        when {
            menu.findItem(R.id.default_choose).isChecked -> files.sort()
            menu.findItem(R.id.size_asc).isChecked -> files.sortWith(compareBy { it.length() })
            menu.findItem(R.id.size_desc).isChecked -> files.sortWith(compareByDescending { it.length() })
            menu.findItem(R.id.date_asc).isChecked -> files.sortWith(compareBy {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Files.readAttributes(
                    it.toPath(),
                    BasicFileAttributes::class.java
                )
                    .creationTime().toMillis() else it.lastModified()
            })

            menu.findItem(R.id.date_desc).isChecked -> files.sortWith(compareByDescending {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Files.readAttributes(
                    it.toPath(),
                    BasicFileAttributes::class.java
                )
                    .creationTime().toMillis() else it.lastModified()
            })

            menu.findItem(R.id.format_asc).isChecked -> files.sortWith(compareBy {
                if (it.isFile) Regex("\\..+").findAll(
                    it.name
                ).last().value else null
            })

            menu.findItem(R.id.format_desc).isChecked -> files.sortWith(compareByDescending {
                if (it.isFile) Regex("\\..+").findAll(
                    it.name
                ).last().value else null
            })
        }
    }

    private fun watchFilesInRecycleView(path: String) {
        binding.noFiles.visibility = View.INVISIBLE
        binding.path.text = path
        val array = File(path).listFiles()
        if (array == null || array.isEmpty()) {
            binding.noFiles.visibility = View.VISIBLE
        } else {
            sortingArrayFiles(array)
        }
        adapterFiles.changeFiles(array ?: arrayOf())
    }

    private fun chooseKindSorting() {
        binding.menu.setNavigationItemSelectedListener {
            it.isChecked = true
            watchFilesInRecycleView(pathToDirectory)
            return@setNavigationItemSelectedListener true
        }
    }

    private fun openNavigationView() {
        binding.toolbar.setNavigationOnClickListener {
            binding.activity.openDrawer(GravityCompat.START)
        }
    }

    private fun goInUpFolder() {
        binding.path.setOnClickListener {
            if ((it as TextView).text != Environment.getExternalStorageDirectory().path) {
                val path = it.text.dropLastWhile { char -> char != File.separatorChar }.dropLast(1)
                    .toString()
                watchFilesInRecycleView(path)
            }
        }
    }

    private fun checkPermission() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestPermission() {
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
            pathToDirectory = file.path
            watchFilesInRecycleView(pathToDirectory)
        } else {
            try {
                FileOpener(this, file.path).opener()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onLongClick(file: File) {

    }

}