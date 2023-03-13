package com.example.mytaxihackathon.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mytaxihackathon.data.local.entity.LocationDto
import com.example.mytaxihackathon.data.local.dao.LocationDao

const val LOCATION_DATABASE_NAME = "Location"

@Database(
    entities = [LocationDto::class],
    version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getLocationDao(): LocationDao
}