package com.dark_composer.simpletask.domain.repository

import com.dark_composer.simpletask.domain.common.Resource
import com.dark_composer.simpletask.domain.model.LocationModel
import kotlinx.coroutines.flow.Flow

interface MainRepository {
    suspend fun getLocations(): Flow<Resource<List<LocationModel>>>
    suspend fun addToList(locationModel: LocationModel)
}