package com.example.filemanager

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.text.format.Formatter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.filemanager.databinding.ItemBinding
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.util.Locale

class FileAdapter(private val context: Activity, private var files: Array<out File>) :
    RecyclerView.Adapter<FileAdapter.FileHolder>() {

    class FileHolder(view: View, private val context: Activity) : ViewHolder(view) {
        private val item = ItemBinding.bind(view)

        fun bind(file: File) {
            item.apply {
                text.text = file.name
                if (file.isDirectory) {
                    icon.setImageResource(R.drawable.folder)
                    val countFilesInDirectory =
                        "${file.listFiles()?.filter { it.isFile }?.size ?: 0} files"
                    size.text = countFilesInDirectory
                } else {
                    icon.setImageResource(icon(file))
                    size.text = Formatter.formatShortFileSize(context, file.length())
                }
                val createdTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
                        .creationTime().toMillis()
                } else {
                    file.lastModified()
                }
                val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
                timeCreated.text = dateFormat.format(createdTime)
                text.isSelected = true
            }
            itemView.setOnClickListener {
                (context as Listener).onClick(file)
            }
            itemView.setOnLongClickListener {
                (context as Listener).onLongClick(file)
                true
            }
        }

        private fun icon(file: File): Int{
            return when (file.name.substringAfterLast(".", "")) {
                "avi" -> R.drawable.avi
                "bmp" -> R.drawable.bmp
                "doc" -> R.drawable.doc
                "txt" -> R.drawable.txt
                "jpg", "jpeg" -> R.drawable.jpg
                "mp3" -> R.drawable.mp3
                "mp4" -> R.drawable.mp4
                "pdf" -> R.drawable.pdf
                "png" -> R.drawable.png
                "wav" -> R.drawable.wav
                else -> R.drawable.file
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return FileHolder(view, context)
    }

    override fun getItemCount() = files.size

    override fun onBindViewHolder(holder: FileHolder, position: Int) {
        holder.bind(files[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeFiles(newFiles: Array<File>) {
        files = newFiles
        notifyDataSetChanged()
    }

    fun pr() {
        files.forEach { Log.e("proverka", it.name) }
    }

    interface Listener {
        fun onClick(file: File)
        fun onLongClick(file: File)
    }
}