package com.example.filemanager

import android.app.Activity
import android.content.Context
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.filemanager.databinding.ItemBinding
import java.io.File

class FileAdapter(val context: Activity, val files: Array<out File>) :
    RecyclerView.Adapter<FileAdapter.FileHolder>() {

    class FileHolder(view: View, val context: Activity) : ViewHolder(view) {
        val item = ItemBinding.bind(view)
        fun bind(file: File) {
            item.apply {
                text.text = file.name
                if (file.isDirectory) {
                    icon.setImageResource(R.drawable.folder)
                    val countFilesInDirectory =
                        "${file.listFiles().filter { it.isFile }.size} files"
                    size.text = countFilesInDirectory
                } else {
                    icon.setImageResource(R.drawable.file)
                    size.text = Formatter.formatShortFileSize(context, file.length())
                }

                text.isSelected = true
            }
            itemView.setOnClickListener {
                (context as Listener).onClick(file)
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

    interface Listener {
        fun onClick(file: File)
    }
}