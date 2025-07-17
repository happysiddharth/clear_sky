package com.weather.clearsky.feature.alerts.data.di

import com.weather.clearsky.feature.alerts.data.repository.AlertRepositoryImpl
import com.weather.clearsky.feature.alerts.domain.repository.AlertRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindAlertRepository(
        alertRepositoryImpl: AlertRepositoryImpl
    ): AlertRepository
} 