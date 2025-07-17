# ClearSky Weather Widget - Architecture Documentation

## Overview

This project follows modern Android development best practices with **Multi-Module Architecture**, **Clean Architecture principles**, **MVVM pattern**, and **Navigation Component** for a scalable, testable, and maintainable codebase.

## 🏗️ Module Structure

```
ClearSky/
├── app/                           # Main application module
├── core/
│   ├── common/                    # Shared utilities and models
│   ├── network/                   # Network layer (API, DTOs, Repositories)
│   └── database/                  # Local storage (Room database)
└── feature/
    ├── main/                      # Main screen feature
    └── weather/                   # Weather widget feature
```

## 📱 Architecture Layers

### 1. **Core Modules**

#### **core:common**
- Domain models (Weather, LocationData, WeatherInfo)
- Shared utilities and extensions
- Resource wrapper for handling API states
- Base classes and interfaces

#### **core:network**
- Retrofit API services
- Network DTOs (Data Transfer Objects)
- Network repositories
- Mappers (DTO ↔ Domain models)
- Hilt dependency injection modules

#### **core:database**
- Room database setup
- DAOs (Data Access Objects)
- Database entities
- Local data repositories

### 2. **Feature Modules**

#### **feature:main**
- Main screen presentation layer
- MainFragment & MainViewModel
- Permission management UI
- Widget management controls

#### **feature:weather**
- Weather widget implementation
- Weather domain logic (Use Cases)
- Location management
- Weather data presentation
- Widget provider and layouts

#### **app**
- Application class with Hilt setup
- Navigation graph
- MainActivity (Navigation host)
- App-level configuration

## 🎯 Design Patterns

### **1. MVVM (Model-View-ViewModel)**

```kotlin
Fragment/Activity → ViewModel → UseCase → Repository → DataSource
```

- **View**: Fragments/Activities observe ViewModel state
- **ViewModel**: Handles UI logic, exposes StateFlow/LiveData
- **Model**: Domain models and business logic

### **2. Repository Pattern**

```kotlin
WeatherNetworkRepository → WeatherApiService → OpenWeatherMap API
```

- Abstracts data sources from business logic
- Provides clean API for data access
- Enables easy testing and data source switching

### **3. Use Case Pattern**

```kotlin
GetCurrentWeatherUseCase → WeatherNetworkRepository
```

- Encapsulates business logic
- Single responsibility principle
- Reusable across different ViewModels

### **4. Dependency Injection (Hilt)**

```kotlin
@HiltViewModel
class WeatherWidgetViewModel @Inject constructor(
    private val getCurrentWeatherUseCase: GetCurrentWeatherUseCase,
    private val locationManager: LocationManager
)
```

- Constructor injection for loose coupling
- Singleton pattern for shared instances
- Easy testing with mock dependencies

## 🧭 Navigation Architecture

### **Navigation Component**

```xml
nav_graph.xml
├── MainFragment (startDestination)
└── [Future: WeatherDetailFragment, SettingsFragment]
```

- Single Activity pattern
- Type-safe navigation with Safe Args
- Centralized navigation logic

## 📊 Data Flow

### **Weather Widget Update Flow**

```
1. WeatherWidgetProvider.onUpdate()
2. → LocationManager.getCurrentLocation()
3. → GetCurrentWeatherUseCase.invoke(location)
4. → WeatherNetworkRepository.getCurrentWeather()
5. → WeatherApiService.getCurrentWeather()
6. → OpenWeatherMap API
7. ← WeatherResponse (DTO)
8. ← Weather (Domain Model)
9. ← WeatherInfo (Display Model)
10. → Update Widget UI
```

### **Permission Management Flow**

```
1. MainFragment requests permissions
2. → MainViewModel.checkPermissions()
3. → LocationManager.hasLocationPermission()
4. ← Permission state
5. → Update UI state
```

## 🔄 State Management

### **Resource Wrapper**

```kotlin
sealed class Resource<out T> {
    object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val exception: Throwable, val message: String?) : Resource<Nothing>()
}
```

- Standardized API response handling
- Loading, Success, Error states
- Type-safe error handling

### **StateFlow/Flow Pattern**

```kotlin
private val _weatherState = MutableStateFlow<Resource<WeatherInfo>>(Resource.Loading)
val weatherState: StateFlow<Resource<WeatherInfo>> = _weatherState.asStateFlow()
```

- Reactive UI updates
- Lifecycle-aware data collection
- Unidirectional data flow

## 🧪 Testing Strategy

### **Unit Testing**
- **ViewModels**: Test business logic and state management
- **Use Cases**: Test business rules and data transformation
- **Repositories**: Test data access and caching logic
- **Mappers**: Test DTO ↔ Domain model conversion

### **Integration Testing**
- **API Services**: Test network layer with mock server
- **Database**: Test Room DAOs with in-memory database
- **End-to-End**: Test complete user flows

### **Dependency Injection for Testing**
```kotlin
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [NetworkModule::class]
)
object FakeNetworkModule {
    @Provides
    @Singleton
    fun provideFakeWeatherRepository(): WeatherNetworkRepository = FakeWeatherRepository()
}
```

## 🔧 Key Technologies

### **Core Libraries**
- **Hilt**: Dependency Injection
- **Navigation Component**: Fragment navigation
- **ViewBinding**: Type-safe view binding
- **Coroutines**: Asynchronous programming
- **StateFlow/Flow**: Reactive programming

### **Network Layer**
- **Retrofit**: REST API client
- **OkHttp**: HTTP client with logging
- **Gson**: JSON serialization

### **Location Services**
- **Google Play Services Location**: Location management
- **FusedLocationProviderClient**: Optimized location access

### **UI Components**
- **Material Design Components**: Modern UI elements
- **CardView**: Card-based layouts
- **ConstraintLayout**: Flexible layouts

## 📋 Benefits of This Architecture

### **1. Separation of Concerns**
- Each module has a single responsibility
- Clear boundaries between layers
- Independent development and testing

### **2. Scalability**
- Easy to add new features as separate modules
- Modular build system for faster compilation
- Team can work on different modules independently

### **3. Testability**
- Dependency injection enables easy mocking
- Pure business logic in Use Cases
- Repository pattern abstracts data sources

### **4. Maintainability**
- Clean Architecture principles
- SOLID principles adherence
- Clear code organization and structure

### **5. Reusability**
- Core modules can be shared across features
- Domain models are framework-agnostic
- Use Cases can be reused in different contexts

## 🚀 Future Enhancements

### **Potential Modules**
- `feature:settings` - App configuration
- `feature:forecast` - Weather forecast feature
- `core:analytics` - Analytics and logging
- `core:preferences` - Shared preferences management

### **Architecture Improvements**
- Room database for offline caching
- WorkManager for background sync
- Paging 3 for large data sets
- Compose UI migration

## 📖 Getting Started

### **API Key Setup**
1. Get free API key from [OpenWeatherMap](https://openweathermap.org/api)
2. Replace `your_api_key_here` in `WeatherNetworkRepository.kt`

### **Build Commands**
```bash
# Build all modules
./gradlew build

# Build specific module
./gradlew :feature:weather:build

# Run tests
./gradlew test

# Install debug APK
./gradlew installDebug
```

### **Module Dependencies**
- App module depends on all feature modules
- Feature modules depend on core modules
- Core modules are independent (except common dependencies)

This architecture provides a solid foundation for a scalable Android application with clear separation of concerns and modern development practices. 