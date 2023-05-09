package com.example.filemanager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import com.example.filemanager.DB.DataBase
import com.example.filemanager.DB.Item
import com.example.filemanager.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes

class MainActivity : AppCompatActivity(), FileAdapter.Listener {
    private var showChangeFile = false
    private lateinit var adapterFiles: FileAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var pathToDirectory: String
    private lateinit var kindSorting: MenuItem
    private var allChangedFiles: Array<File> = arrayOf()
    private val dataBase by lazy { DataBase.getDB(this) }
    private val savePath: ViewModelForPath by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        kindSorting = binding.menu.menu.findItem(R.id.default_choose)
        setSupportActionBar(binding.toolbar)
        requestPermission()
        binding.path.isSelected = true
        pathToDirectory =
            savePath.liveDataPath.value ?: Environment.getExternalStorageDirectory().path
        binding.path.text = pathToDirectory
        adapterFiles = FileAdapter(this, File(pathToDirectory).listFiles() ?: arrayOf())
        binding.fileList.adapter = adapterFiles
        watchFilesInRecycleView(File(pathToDirectory).listFiles())
        openNavigationView()
        goInUpFolder()
        chooseKindSorting()
        savePath.liveDataPath.observe(this) {
            binding.path.text = it
            pathToDirectory = it
        }
        hashCodeAllFiles()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_change, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        showChangeFile = !showChangeFile
        watchFilesInRecycleView(if (showChangeFile) allChangedFiles else File(pathToDirectory).listFiles())
        return true
    }

    private fun sortingArrayFiles(files: Array<File>) {
        val menu = binding.menu.menu
        when (kindSorting) {
            menu.findItem(R.id.default_choose) -> files.sort()
            menu.findItem(R.id.size_asc) -> files.sortWith(compareBy { it.length() })
            menu.findItem(R.id.size_desc) -> files.sortWith(compareByDescending { it.length() })
            menu.findItem(R.id.date_asc) -> files.sortWith(compareBy {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Files.readAttributes(
                    it.toPath(),
                    BasicFileAttributes::class.java
                )
                    .creationTime().toMillis() else it.lastModified()
            })

            menu.findItem(R.id.date_desc) -> files.sortWith(compareByDescending {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Files.readAttributes(
                    it.toPath(),
                    BasicFileAttributes::class.java
                )
                    .creationTime().toMillis() else it.lastModified()
            })

            menu.findItem(R.id.format_asc) -> files.sortWith(compareBy {
                if (it.isFile) Regex("\\..+").findAll(
                    it.name
                ).last().value else null
            })

            menu.findItem(R.id.format_desc) -> files.sortWith(compareByDescending {
                if (it.isFile) Regex("\\..+").findAll(
                    it.name
                ).last().value else null
            })
        }
    }

    private fun watchFilesInRecycleView(array: Array<File>?) {
        binding.noFiles.visibility = View.INVISIBLE
        if (array == null || array.isEmpty()) {
            binding.noFiles.visibility = View.VISIBLE
        } else {
            sortingArrayFiles(array)
        }
        adapterFiles.changeFiles(array ?: arrayOf())
    }

    private fun chooseKindSorting() {
        binding.menu.setNavigationItemSelectedListener {
            kindSorting.isChecked = false
            it.isChecked = true
            kindSorting = it
            watchFilesInRecycleView(if (showChangeFile) allChangedFiles else File(pathToDirectory).listFiles())
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
                savePath.liveDataPath.value = path
                watchFilesInRecycleView(File(pathToDirectory).listFiles())
            }
        }
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        )
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }
    }

    override fun onClick(file: File) {
        if (file.isDirectory) {
            savePath.liveDataPath.value = file.path
            watchFilesInRecycleView(File(pathToDirectory).listFiles())
        } else {
            try {
                FileOpener(this, file.path).opener()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onLongClick(file: File) {
        val share = Intent(Intent.ACTION_SEND)
        val fileUri = FileProvider.getUriForFile(
            this,
            applicationContext.packageName + ".provider",
            file
        )
        share.type = "application/*"
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        share.putExtra(Intent.EXTRA_STREAM, fileUri)
        startActivity(Intent.createChooser(share, "Share file"))
    }

    private fun hashCodeAllFiles() {
        Thread {
            allChangedFiles =
                allChangedFiles(File(Environment.getExternalStorageDirectory().path)).toTypedArray()
        }.start()
    }

    private fun allChangedFiles(directory: File): MutableList<File> {
        val listFiles = mutableListOf<File>()
        if (directory.isDirectory) {
            val inDirectory = directory.listFiles() ?: arrayOf<File>()
            for (file in inDirectory) {
                listFiles.addAll(allChangedFiles(file))
            }
        } else {
            val hash = dataBase.workWithData().getItem(directory.path)
            if (hash.isEmpty()) {
                dataBase.workWithData()
                    .insertItem(Item(null, directory.path, directory.hashCode().toString()))
            } else if (hash.first().hash != directory.hashCode().toString()) {
                listFiles.add(directory)
                dataBase.workWithData()
                    .updateItem(Item(null, directory.path, directory.hashCode().toString()))
            }
        }
        return listFiles
    }
}