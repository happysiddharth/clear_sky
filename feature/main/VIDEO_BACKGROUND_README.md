# Video Background System

This document explains how to add and manage video backgrounds for different weather conditions in the ClearSky app.

## 📁 Video File Location

Place your video files in: `feature/main/src/main/res/raw/`

## 🎬 Required Video Files

The following video files are needed for complete weather condition coverage:

### Core Weather Videos
- `weather_rain.mp4` - Rain, drizzle, showers
- `weather_thunderstorm.mp4` - Thunderstorms, lightning
- `weather_snow.mp4` - Snow, sleet, blizzard
- `weather_sunny.mp4` - Clear skies, sunny weather
- `weather_cloudy.mp4` - Cloudy, overcast, partly cloudy
- `weather_foggy.mp4` - Mist, fog, haze
- `weather_night.mp4` - Clear night skies
- `weather_windy.mp4` - Windy conditions, storms
- `weather_default.mp4` - Fallback video for unknown conditions

## 📐 Video Specifications

### Recommended Format
- **Format**: MP4 (H.264)
- **Resolution**: 1080p (1920x1080) or higher
- **Aspect Ratio**: 16:9 or vertical (9:16) for mobile optimization
- **Duration**: 10-30 seconds (will loop automatically)
- **File Size**: Under 5MB per video for optimal performance

### Technical Requirements
- **No Audio**: Videos should be silent (audio is automatically muted)
- **Seamless Loop**: Ensure the end frame transitions smoothly to the beginning
- **High Quality**: Use good compression to balance quality and file size
- **Weather Appropriate**: Videos should clearly represent the weather condition

## 🎨 Video Content Suggestions

### Rain (`weather_rain.mp4`)
- Raindrops on windows
- Rain falling in nature
- Puddles with ripples
- Urban rain scenes

### Thunderstorm (`weather_thunderstorm.mp4`)
- Lightning flashes
- Dark stormy clouds
- Heavy rain with lightning
- Dramatic storm scenes

### Snow (`weather_snow.mp4`)
- Snowflakes falling
- Snow-covered landscapes
- Blizzard conditions
- Gentle snowfall

### Sunny (`weather_sunny.mp4`)
- Blue skies with clouds
- Sunlight through trees
- Golden hour lighting
- Bright daylight scenes

### Cloudy (`weather_cloudy.mp4`)
- Moving clouds
- Overcast skies
- Time-lapse cloud movement
- Partly cloudy conditions

### Foggy (`weather_foggy.mp4`)
- Misty landscapes
- Fog rolling over terrain
- Low visibility conditions
- Atmospheric haze

### Night (`weather_night.mp4`)
- Clear starry skies
- City lights at night
- Moon and stars
- Peaceful night scenes

### Windy (`weather_windy.mp4`)
- Trees swaying in wind
- Leaves blowing
- Grass moving in breeze
- Wind effects on water

### Default (`weather_default.mp4`)
- Generic pleasant weather
- Slow cloud movement
- Neutral sky conditions
- Universally appealing scene

## 🔧 Implementation Details

### Automatic Mapping
The `WeatherVideoMapper` class automatically maps weather conditions to videos:

```kotlin
// By weather condition string
WeatherVideoMapper.getWeatherVideo("Rain") // Returns R.raw.weather_rain

// By weather icon code  
WeatherVideoMapper.getWeatherVideoByIcon("10d") // Returns R.raw.weather_rain
```

### Video Playback Features
- **Auto-loop**: Videos loop continuously
- **Muted**: No audio playback
- **Optimized**: Scaled to fit with cropping
- **Fallback**: Falls back to gradient if video fails
- **Lifecycle-aware**: Pauses/resumes with app state

## 🎯 Video Sources

### Free Video Resources
- **Pixabay**: https://pixabay.com/videos/
- **Pexels**: https://www.pexels.com/videos/
- **Unsplash**: https://unsplash.com/backgrounds/nature/weather
- **Videvo**: https://www.videvo.net/

### Search Keywords
- "rain loop video"
- "thunderstorm time lapse"
- "snow falling loop"
- "sunny sky clouds"
- "fog mist atmosphere"
- "night sky stars"

## 🚀 Testing

To test the video background system:

1. Add at least `weather_default.mp4` to test basic functionality
2. Launch the app and observe the hero section background
3. The video should auto-play, loop, and be muted
4. Weather condition changes should trigger video changes

## ⚡ Performance Tips

- Keep video files under 5MB each
- Use efficient video compression
- Test on lower-end devices
- Monitor memory usage with multiple videos
- Consider using shorter loops (10-15 seconds)

## 🐛 Troubleshooting

### Video Not Playing
- Check file format (MP4 recommended)
- Verify file is in `res/raw/` directory
- Ensure filename matches mapper expectations
- Check device video codec support

### Performance Issues
- Reduce video file sizes
- Lower video resolution
- Shorter loop duration
- Test on target devices

### Layout Issues
- Video scales to fit with cropping
- Text overlay remains readable with dark overlay
- Fallback gradient shows if video fails 