package com.weather.clearsky.core.common.navigation

import android.content.Intent
import android.net.Uri
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized navigation manager that handles all navigation in the app.
 * Features can register their navigation handlers with this manager.
 */
@Singleton
class NavigationManager @Inject constructor() {
    
    private var navController: NavController? = null
    private val featureNavigators = mutableMapOf<String, FeatureNavigator>()
    private val routeRegistry = mutableMapOf<String, Int>()
    
    // Events for navigation that can't be handled directly
    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()
    
    /**
     * Set the navigation controller (called from MainActivity)
     */
    fun setNavController(navController: NavController) {
        this.navController = navController
    }
    
    /**
     * Register a feature navigator
     */
    fun registerFeatureNavigator(feature: String, navigator: FeatureNavigator) {
        featureNavigators[feature] = navigator
    }
    
    /**
     * Register a route with its resource ID
     */
    fun registerRoute(routeId: String, resourceId: Int) {
        routeRegistry[routeId] = resourceId
    }
    
    /**
     * Navigate to a specific route
     */
    suspend fun navigateTo(
        route: NavigationRoute,
        options: NavigationOptions = NavigationOptions.DEFAULT
    ): NavigationResult {
        return try {
            when (route) {
                is NavigationRoute.ExternalUrl -> {
                    handleExternalUrl(route.arguments.getString("url") ?: "")
                    NavigationResult.Success
                }
                is NavigationRoute.DeepLink -> {
                    handleDeepLink(route.arguments.getString("uri") ?: "")
                    NavigationResult.Success
                }
                else -> {
                    navigateInternal(route, options)
                }
            }
        } catch (e: Exception) {
            NavigationResult.Error(e.message ?: "Navigation failed")
        }
    }
    
    /**
     * Navigate back
     */
    fun navigateBack(): Boolean {
        return navController?.navigateUp() ?: false
    }
    
    /**
     * Check if we can navigate back
     */
    fun canNavigateBack(): Boolean {
        return navController?.previousBackStackEntry != null
    }
    
    /**
     * Get current destination route
     */
    fun getCurrentRoute(): String? {
        return navController?.currentDestination?.route
    }
    
    private suspend fun navigateInternal(
        route: NavigationRoute,
        options: NavigationOptions
    ): NavigationResult {
        val navController = this.navController ?: return NavigationResult.Error("NavController not set")
        
        // Check if any feature navigator can handle this route
        featureNavigators.values.forEach { navigator ->
            if (navigator.canHandle(route)) {
                return navigator.navigate(navController, route, options)
            }
        }
        
        // Fall back to resource ID navigation
        val resourceId = routeRegistry[route.routeId]
        if (resourceId != null) {
            navController.navigate(resourceId, route.arguments)
            return NavigationResult.Success
        }
        
        return NavigationResult.Error("No navigator found for route: ${route.routeId}")
    }
    
    private suspend fun handleExternalUrl(url: String) {
        _navigationEvents.emit(NavigationEvent.OpenExternalUrl(url))
    }
    
    private suspend fun handleDeepLink(uri: String) {
        _navigationEvents.emit(NavigationEvent.HandleDeepLink(Uri.parse(uri)))
    }
}

/**
 * Interface for feature-specific navigators
 */
interface FeatureNavigator {
    fun canHandle(route: NavigationRoute): Boolean
    suspend fun navigate(
        navController: NavController,
        route: NavigationRoute,
        options: NavigationOptions
    ): NavigationResult
}

/**
 * Result of a navigation operation
 */
sealed class NavigationResult {
    data object Success : NavigationResult()
    data class Error(val message: String) : NavigationResult()
}

/**
 * Events that need to be handled at the Activity level
 */
sealed class NavigationEvent {
    data class OpenExternalUrl(val url: String) : NavigationEvent()
    data class HandleDeepLink(val uri: Uri) : NavigationEvent()
    data class ShowDialog(val dialogInfo: DialogInfo) : NavigationEvent()
}

/**
 * Information for showing dialogs
 */
data class DialogInfo(
    val title: String,
    val message: String,
    val positiveButton: String? = null,
    val negativeButton: String? = null,
    val onPositive: (() -> Unit)? = null,
    val onNegative: (() -> Unit)? = null
) 