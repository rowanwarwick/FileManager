package com.example.filemanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.filemanager.databinding.ItemBinding
import java.io.File

class FileAdapter(val files: Array<out File>, val listener: Listener?) :
    RecyclerView.Adapter<FileAdapter.FileHolder>() {

    class FileHolder(view: View, val listener: Listener?) : ViewHolder(view) {
        val item = ItemBinding.bind(view)
        fun bind(file: File) {
            item.apply {
                text.text = file.name
                if (file.isDirectory) {
                    icon.setImageResource(R.drawable.folder)
                } else {
                    icon.setImageResource(R.drawable.file)
                }
            }
            itemView.setOnClickListener {
                listener?.onClick(file)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return FileHolder(view, listener)
    }

    override fun getItemCount() = files.size

    override fun onBindViewHolder(holder: FileHolder, position: Int) {
        holder.bind(files[position])
    }

    interface Listener {
        fun onClick(file: File)
    }
}