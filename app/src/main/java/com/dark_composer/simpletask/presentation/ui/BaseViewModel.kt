package com.dark_composer.simpletask.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dark_composer.simpletask.domain.common.Resource
import com.dark_composer.simpletask.presentation.common.UIListState
import com.dark_composer.simpletask.presentation.common.UIObjectState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    fun <T> loadList (
        repositoryCall: suspend () -> Flow<Resource<List<T>>>,
    ): Flow<UIListState<T>> {
        val listState = MutableStateFlow(UIListState<T>())

        viewModelScope.launch(Dispatchers.IO) {
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

    fun <T> load(
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