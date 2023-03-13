package com.example.mytaxihackathon.data.local.dao

import androidx.room.*
import com.example.mytaxihackathon.data.local.database.LOCATION_DATABASE_NAME
import com.example.mytaxihackathon.data.local.entity.LocationDto

@Dao
interface LocationDao {

    @Query("SELECT * FROM $LOCATION_DATABASE_NAME")
    fun getLocation(): List<LocationDto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun setLocation(users: List<LocationDto>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun setLocation(user: LocationDto)

    @Delete
    fun deleteLocation(user: LocationDto)

    @Query("DELETE FROM $LOCATION_DATABASE_NAME")
    fun deleteAllLocation()

}