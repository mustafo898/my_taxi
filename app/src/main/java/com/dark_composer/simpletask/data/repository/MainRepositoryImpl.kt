package com.dark_composer.simpletask.data.repository

import com.dark_composer.simpletask.data.local.dao.LocationDao
import com.dark_composer.simpletask.data.mapper.toDto
import com.dark_composer.simpletask.data.mapper.toModel
import com.dark_composer.simpletask.domain.common.Resource
import com.dark_composer.simpletask.domain.model.LocationModel
import com.dark_composer.simpletask.domain.repository.MainRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MainRepositoryImpl @Inject constructor(
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