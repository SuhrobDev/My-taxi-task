package com.example.mytaxihackathon.domain.repository

import com.example.mytaxihackathon.domain.common.Resource
import com.example.mytaxihackathon.domain.model.LocationModel
import kotlinx.coroutines.flow.Flow

interface MainRepository {
    suspend fun getLocations(): Flow<Resource<List<LocationModel>>>
    suspend fun addToList(locationModel: LocationModel)
}