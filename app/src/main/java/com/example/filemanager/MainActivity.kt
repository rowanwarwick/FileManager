package com.example.filemanager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.KeyEvent
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
import com.example.filemanager.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity(), FileAdapter.Listener {
    private lateinit var adapterFiles: FileAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var pathToDirectory: String
    private lateinit var kindSorting: MenuItem
    private val workerWithHash by lazy { WorkerWithHash(DataBase.getDB(this)) }
    private val savePath: ViewModelForPath by viewModels()
    private var showChangeFile = false

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
        workerWithHash.hashCodeAllFiles()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_change, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        showChangeFile = !showChangeFile
        watchFilesInRecycleView(if (showChangeFile) workerWithHash.allChangedFiles else File(pathToDirectory).listFiles())
        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && binding.path.text != Environment.getExternalStorageDirectory().path) {
            val path =
                binding.path.text.dropLastWhile { char -> char != File.separatorChar }.dropLast(1)
                    .toString()
            savePath.liveDataPath.value = path
            watchFilesInRecycleView(File(pathToDirectory).listFiles())
            return true
        }
        return super.onKeyDown(keyCode, event)
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

    private fun watchFilesInRecycleView(array: Array<File>?) {
        binding.noFiles.visibility = View.INVISIBLE
        if (array.isNullOrEmpty()) {
            binding.noFiles.visibility = View.VISIBLE
        }
        adapterFiles.changeFiles(array ?: arrayOf(), binding.menu.menu, kindSorting)
    }

    private fun chooseKindSorting() {
        binding.menu.setNavigationItemSelectedListener {
            kindSorting.isChecked = false
            it.isChecked = true
            kindSorting = it
            watchFilesInRecycleView(if (showChangeFile) workerWithHash.allChangedFiles else File(pathToDirectory).listFiles())
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
}