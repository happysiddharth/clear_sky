package com.weather.clearsky.feature.main.navigation

import androidx.navigation.NavController
import com.weather.clearsky.core.common.navigation.FeatureNavigator
import com.weather.clearsky.core.common.navigation.NavigationOptions
import com.weather.clearsky.core.common.navigation.NavigationResult
import com.weather.clearsky.core.common.navigation.NavigationRoute
import javax.inject.Inject

/**
 * Navigator for the Main feature module
 */
class MainFeatureNavigator @Inject constructor() : FeatureNavigator {
    
    override fun canHandle(route: NavigationRoute): Boolean {
        return when (route) {
            is NavigationRoute.Main -> true
            else -> false
        }
    }
    
    override suspend fun navigate(
        navController: NavController,
        route: NavigationRoute,
        options: NavigationOptions
    ): NavigationResult {
        return try {
            when (route) {
                is NavigationRoute.Main -> {
                    if (options.clearBackStack) {
                        navController.popBackStack()
                    }
                    NavigationResult.Success
                }
                else -> NavigationResult.Error("Route not supported by MainFeatureNavigator")
            }
        } catch (e: Exception) {
            NavigationResult.Error(e.message ?: "Navigation failed in MainFeatureNavigator")
        }
    }
} 