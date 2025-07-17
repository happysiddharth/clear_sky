package com.weather.clearsky.feature.main.di

import com.weather.clearsky.core.common.navigation.FeatureNavigator
import com.weather.clearsky.feature.main.navigation.MainFeatureNavigator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class MainNavigationModule {
    
    @Binds
    @IntoSet
    abstract fun bindMainFeatureNavigator(
        mainFeatureNavigator: MainFeatureNavigator
    ): FeatureNavigator
} 