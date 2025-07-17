package com.weather.clearsky.feature.weather.di

import com.weather.clearsky.core.common.navigation.FeatureNavigator
import com.weather.clearsky.feature.weather.navigation.WeatherFeatureNavigator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class WeatherNavigationModule {
    
    @Binds
    @IntoSet
    abstract fun bindWeatherFeatureNavigator(
        weatherFeatureNavigator: WeatherFeatureNavigator
    ): FeatureNavigator
} 