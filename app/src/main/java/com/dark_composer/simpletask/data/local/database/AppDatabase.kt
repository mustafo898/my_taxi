package com.dark_composer.simpletask.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dark_composer.simpletask.data.local.dao.LocationDao
import com.dark_composer.simpletask.data.local.entity.LocationDto

@Database(
    entities = [LocationDto::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
}