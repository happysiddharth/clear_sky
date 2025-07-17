package com.weather.clearsky.feature.settings.di

import com.weather.clearsky.core.common.navigation.FeatureNavigator
import com.weather.clearsky.feature.settings.navigation.SettingsFeatureNavigator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsNavigationModule {
    
    @Binds
    @IntoSet
    abstract fun bindSettingsFeatureNavigator(
        settingsFeatureNavigator: SettingsFeatureNavigator
    ): FeatureNavigator
} 