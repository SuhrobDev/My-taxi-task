package com.example.mytaxihackathon.data.repository

import com.example.mytaxihackathon.data.local.dao.LocationDao
import com.example.mytaxihackathon.data.mapper.toDto
import com.example.mytaxihackathon.data.mapper.toModel
import com.example.mytaxihackathon.domain.common.Resource
import com.example.mytaxihackathon.domain.model.LocationModel
import com.example.mytaxihackathon.domain.repository.MainRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MainRepositoryImpl(
    private val locationDao: LocationDao,
) : MainRepository {
    override suspend fun getLocations(): Flow<Resource<List<LocationModel>>> {
        return flow {
            emit(Resource.Success(locationDao.getLocation().map { it.toModel() }))
        }
    }

    override suspend fun addToList(locationModel: LocationModel) {
        locationDao.setLocation(locationModel.toDto())
    }

}