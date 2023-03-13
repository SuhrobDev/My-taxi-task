package com.example.mytaxihackathon.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mytaxihackathon.domain.common.Resource
import com.example.mytaxihackathon.presentation.common.UIListState
import com.example.mytaxihackathon.presentation.common.UIObjectState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel : ViewModel() {

    protected fun launchScope(
        context: CoroutineContext = Dispatchers.IO,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch(context, start, block)
    }

    fun <T> getDataList(
        repositoryCall: suspend () -> Flow<Resource<List<T>>>,
    ): Flow<UIListState<T>> {
        val listState = MutableStateFlow(UIListState<T>())
        viewModelScope.launch {
            repositoryCall().onEach {
                when (it) {
                    is Resource.Error -> {
                        listState.value = UIListState(it.message ?: "")
                    }
                    is Resource.Loading -> {
                        listState.value = UIListState(isLoading = true)
                    }
                    is Resource.Success -> {
                        listState.value = UIListState(data = it.data)
                    }
                }
            }.launchIn(CoroutineScope(Dispatchers.IO))
        }

        return listState.asStateFlow()
    }

    fun <T> getDataObject(
        repositoryCall: suspend () -> Flow<Resource<T>>,
        listState: MutableStateFlow<UIObjectState<T>>
    ) {
        viewModelScope.launch {
            repositoryCall().onEach {
                when (it) {
                    is Resource.Error -> {
                        listState.value = UIObjectState(it.message ?: "")
                    }
                    is Resource.Loading -> {
                        listState.value = UIObjectState(isLoading = true)
                    }
                    is Resource.Success -> {
                        listState.value = UIObjectState(data = it.data)
                    }
                }
            }.launchIn(CoroutineScope(Dispatchers.IO))
        }
    }

}