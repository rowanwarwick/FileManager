package com.example.filemanager.DB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Item::class], version = 1, exportSchema = false)
abstract class DataBase : RoomDatabase() {
    abstract fun workWithData(): DataAccessObject

    companion object {
        fun getDB(context: Context) =
            Room.databaseBuilder(context.applicationContext, DataBase::class.java, "FileManager.db")
                .build()
    }
}