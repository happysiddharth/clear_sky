# Clear Sky Weather Widget

A beautiful Android weather widget that displays current weather information for your location.

## Features

- 🌡️ Current temperature display
- 🌤️ Weather condition with emoji icons
- 📍 Location-based weather data
- 💧 Humidity information
- 🌡️ "Feels like" temperature
- 🔄 Tap to refresh functionality
- 🎨 Modern gradient design

## Setup Instructions

### 1. Get OpenWeatherMap API Key

1. Visit [OpenWeatherMap](https://openweathermap.org/api)
2. Sign up for a free account
3. Navigate to API Keys section
4. Copy your API key

### 2. Configure the App

1. Open `app/src/main/java/com/weather/clearsky/network/WeatherRepository.kt`
2. Replace `"your_api_key_here"` with your actual API key:
   ```kotlin
   private const val API_KEY = "your_actual_api_key_here"
   ```

### 3. Install and Setup

1. Build and install the app on your device
2. Open the Clear Sky app
3. Grant location permissions when prompted
4. Tap "Add Widget to Home Screen"
5. Follow the instructions to add the widget to your home screen

## Usage

- **Adding Widget**: Use the main app to add the widget to your home screen
- **Refreshing**: Tap on the "Tap to refresh" text at the bottom of the widget
- **Permissions**: Grant location permissions for accurate local weather
- **Manual Refresh**: Use the main app to refresh all widgets at once

## Widget Features

- **Size**: 4x2 grid cells (approximately 250dp x 110dp)
- **Update Frequency**: Automatically updates every 30 minutes
- **Location Fallback**: Uses London as default if location is unavailable
- **Error Handling**: Displays error messages for network issues

## Permissions

- `ACCESS_FINE_LOCATION` - For precise location-based weather
- `ACCESS_COARSE_LOCATION` - For approximate location-based weather
- `INTERNET` - For API calls to fetch weather data
- `ACCESS_NETWORK_STATE` - To check network connectivity

## Architecture

- **MVVM Pattern**: Clean separation of concerns
- **Retrofit**: For API communication
- **Coroutines**: For asynchronous operations
- **Location Services**: Google Play Services for location
- **Material Design**: Modern UI components

## Troubleshooting

### Widget Not Updating
- Check internet connection
- Verify API key is correctly set
- Grant location permissions
- Try manual refresh from main app

### Location Not Working
- Enable GPS/Location services
- Grant location permissions
- Check if location services are available

### API Errors
- Verify API key is valid
- Check API usage limits
- Ensure internet connectivity

## Customization

You can customize the widget appearance by modifying:
- `res/layout/weather_widget.xml` - Widget layout
- `res/drawable/widget_background.xml` - Background gradient
- `WeatherWidgetProvider.kt` - Update logic and intervals

## API Information

This app uses the OpenWeatherMap Current Weather Data API:
- **Free Tier**: 1,000 calls/day, 60 calls/minute
- **Data**: Current weather conditions
- **Coverage**: Worldwide
- **Updates**: Real-time data

## License

This project is open source and available under the MIT License. 