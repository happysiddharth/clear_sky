package com.weather.clearsky.feature.alerts.data.local.di

import android.content.Context
import com.weather.clearsky.feature.alerts.data.local.dao.WeatherAlertDao
import com.weather.clearsky.feature.alerts.data.local.database.AlertsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAlertsDatabase(@ApplicationContext context: Context): AlertsDatabase {
        return AlertsDatabase.getDatabase(context)
    }
    
    @Provides
    fun provideWeatherAlertDao(database: AlertsDatabase): WeatherAlertDao {
        return database.weatherAlertDao()
    }
} 