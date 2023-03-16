package com.dark_composer.simpletask.presentation.ui.main

import androidx.lifecycle.viewModelScope
import com.dark_composer.simpletask.domain.model.LocationModel
import com.dark_composer.simpletask.domain.repository.MainRepository
import com.dark_composer.simpletask.presentation.common.UIListState
import com.dark_composer.simpletask.presentation.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: MainRepository) : BaseViewModel() {
    suspend fun addToList(locationModel: LocationModel) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addToList(locationModel)
        }
    }

    suspend fun getLocations(): kotlinx.coroutines.flow.Flow<UIListState<LocationModel>> {
        return loadList { repository.getLocations() }
    }
}