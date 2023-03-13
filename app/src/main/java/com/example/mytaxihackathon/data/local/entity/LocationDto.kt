package com.example.mytaxihackathon.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mytaxihackathon.data.local.database.LOCATION_DATABASE_NAME

@Entity(tableName = LOCATION_DATABASE_NAME)
data class LocationDto(
    @PrimaryKey
    val id: Double,
    val latitude: Double,
    val longitude: Double,
)