package com.example.filemanager.DB

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DataAccessObject {
    @Insert
    fun insertItem(item: Item)
    @Query("SELECT * FROM hashValueFiles WHERE path = :path")
    fun getItem(path: String): List<Item>
    @Update
    fun updateItem(item: Item)
}