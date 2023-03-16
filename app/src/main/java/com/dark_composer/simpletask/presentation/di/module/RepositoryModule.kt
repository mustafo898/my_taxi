package com.dark_composer.simpletask.presentation.di.module

import com.dark_composer.simpletask.data.local.dao.LocationDao
import com.dark_composer.simpletask.data.repository.MainRepositoryImpl
import com.dark_composer.simpletask.domain.repository.MainRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMainRepository(
        locationDao: LocationDao
    ): MainRepository {
        return MainRepositoryImpl(locationDao)
    }
}