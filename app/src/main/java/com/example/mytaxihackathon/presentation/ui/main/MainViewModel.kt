package com.example.mytaxihackathon.presentation.ui.main

import androidx.lifecycle.viewModelScope
import com.example.mytaxihackathon.domain.model.LocationModel
import com.example.mytaxihackathon.domain.repository.MainRepository
import com.example.mytaxihackathon.presentation.common.UIListState
import com.example.mytaxihackathon.presentation.ui.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: MainRepository
) : BaseViewModel() {

    suspend fun addToList(locationModel: LocationModel) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addToList(locationModel)
        }
    }

    suspend fun getLocations(): Flow<UIListState<LocationModel>> {
        return getDataList {
            repository.getLocations()
        }
    }

}