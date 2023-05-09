package com.example.filemanager.DB

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "hashValueFiles")
data class Item(
    @PrimaryKey(false)
    val index: Int? = null,
    @ColumnInfo("path")
    val path: String,
    @ColumnInfo("hash")
    val hash: String
)
