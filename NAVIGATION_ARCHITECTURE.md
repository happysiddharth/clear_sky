# 🧭 **Navigation Architecture Guide**

## **Overview**
We've implemented a **scalable, type-safe navigation architecture** using the **Navigation Manager Pattern** with **Feature Registry**. This design makes it extremely easy to add new features while maintaining clean separation of concerns.

---

## **🏗️ Architecture Components**

### **1. NavigationRoute (Sealed Class)**
```kotlin
sealed class NavigationRoute(val routeId: String, val arguments: Bundle = Bundle())

// Examples:
NavigationRoute.Settings
NavigationRoute.WeatherLocation("location123")
NavigationRoute.ExternalUrl("https://example.com")
```

**Benefits:**
- ✅ **Type-safe navigation** - compile-time safety
- ✅ **Extensible** - easy to add new routes
- ✅ **Parameter support** - pass data with navigation

### **2. NavigationManager (Centralized Controller)**
```kotlin
@Singleton
class NavigationManager {
    suspend fun navigateTo(route: NavigationRoute, options: NavigationOptions)
    fun navigateBack(): Boolean
    fun registerFeatureNavigator(feature: String, navigator: FeatureNavigator)
}
```

**Features:**
- 🎯 **Single source of truth** for navigation
- 🔄 **Feature navigator registration**
- 📱 **Handle external URLs & deep links**
- ⚡ **Coroutine-based** for async operations

### **3. FeatureNavigator (Modular Handlers)**
```kotlin
interface FeatureNavigator {
    fun canHandle(route: NavigationRoute): Boolean
    suspend fun navigate(navController: NavController, route: NavigationRoute, options: NavigationOptions): NavigationResult
}
```

**Each feature implements its own navigator:**
- `MainFeatureNavigator` - handles main app navigation
- `SettingsFeatureNavigator` - handles settings navigation  
- `WeatherFeatureNavigator` - handles weather-related navigation

---

## **🚀 How to Add New Features**

### **Step 1: Add Route to NavigationRoute.kt**
```kotlin
// Add to NavigationRoute sealed class
data object Profile : NavigationRoute("profile")
data class UserProfile(val userId: String) : NavigationRoute(
    "user_profile",
    Bundle().apply { putString("user_id", userId) }
)
```

### **Step 2: Create Feature Navigator**
```kotlin
// feature/profile/navigation/ProfileFeatureNavigator.kt
class ProfileFeatureNavigator @Inject constructor() : FeatureNavigator {
    
    override fun canHandle(route: NavigationRoute): Boolean {
        return when (route) {
            is NavigationRoute.Profile -> true
            is NavigationRoute.UserProfile -> true
            else -> false
        }
    }
    
    override suspend fun navigate(/* ... */): NavigationResult {
        // Handle navigation within profile feature
    }
}
```

### **Step 3: Register Navigator with Hilt**
```kotlin
// feature/profile/di/ProfileNavigationModule.kt
@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileNavigationModule {
    
    @Binds
    @IntoSet
    abstract fun bindProfileFeatureNavigator(
        navigator: ProfileFeatureNavigator
    ): FeatureNavigator
}
```

### **Step 4: Use in Fragments**
```kotlin
// From any fragment:
navigateTo(navigationManager, CommonRoutes.toProfile())

// Or with parameters:
navigateTo(navigationManager, NavigationRoute.UserProfile("user123"))
```

---

## **📱 Usage Examples**

### **Simple Navigation**
```kotlin
// Navigate to settings
binding.settingsButton.setOnClickListener {
    navigateTo(navigationManager, CommonRoutes.toSettings())
}
```

### **Navigation with Parameters**
```kotlin
// Navigate to specific weather location
binding.locationItem.setOnClickListener {
    navigateTo(navigationManager, NavigationRoute.WeatherLocation(locationId))
}
```

### **Navigation with Options**
```kotlin
// Navigate and clear back stack
navigateTo(
    navigationManager, 
    CommonRoutes.toMain(),
    NavigationOptions.CLEAR_STACK
)
```

### **External Navigation**
```kotlin
// Open external URL
navigateTo(navigationManager, CommonRoutes.toExternalUrl("https://google.com"))

// Handle deep link
navigateTo(navigationManager, CommonRoutes.toDeepLink("myapp://weather/details"))
```

---

## **🎯 Benefits of This Architecture**

### **🔒 Type Safety**
- Compile-time checking of navigation routes
- No magic strings or resource IDs in feature modules
- Parameter validation at compile time

### **🧩 Modularity**
- Each feature owns its navigation logic
- Zero coupling between feature modules
- Easy to test individual navigators

### **📈 Scalability**
- Add new features without touching existing code
- Automatic registration via Hilt
- Consistent navigation patterns

### **🔄 Maintainability**
- Single place to handle navigation events
- Clear separation of concerns
- Easy to debug navigation flows

### **⚡ Performance**
- Lazy loading of feature navigators
- Coroutine-based for smooth UX
- Minimal overhead

---

## **🛠️ Current Feature Support**

### **✅ Implemented Features**
- 🏠 **Main** - Dashboard and core functionality
- ⚙️ **Settings** - App configuration
- 🌤️ **Weather** - Weather data and widgets

### **🔮 Ready-to-Implement Routes**
- 📅 **Forecast** - Extended weather forecasts
- 🗺️ **Maps** - Location-based weather maps
- 👤 **Profile** - User profile management
- 🔔 **Notifications** - Push notification settings

---

## **🎉 Example: Adding a Maps Feature**

1. **Add route:**
```kotlin
data object Maps : NavigationRoute("maps")
```

2. **Create navigator:**
```kotlin
class MapsFeatureNavigator @Inject constructor() : FeatureNavigator { /* ... */ }
```

3. **Register with Hilt:**
```kotlin
@Binds @IntoSet abstract fun bindMapsNavigator(navigator: MapsFeatureNavigator): FeatureNavigator
```

4. **Use anywhere:**
```kotlin
navigateTo(navigationManager, CommonRoutes.toMaps())
```

**That's it!** 🎉 Your new feature is fully integrated with the navigation system.

---

## **📁 File Structure**
```
core/common/navigation/
├── NavigationRoute.kt          # Route definitions
├── NavigationManager.kt        # Central controller
├── NavigationModule.kt         # Hilt module
└── NavigationExtensions.kt     # Convenience functions

feature/*/navigation/
├── *FeatureNavigator.kt        # Feature-specific navigator
└── ../di/*NavigationModule.kt  # Feature navigator registration

app/
└── MainActivity.kt             # NavigationManager setup
```

This architecture provides a **solid foundation** for your app's navigation that will **scale beautifully** as you add more features! 🚀 