# 🔍 **Debug Tools Integration Guide**

## **🛠️ Integrated Debug Tools**

We've integrated **Pluto** and **LeakCanary** to supercharge your debugging experience during development.

---

## **🚀 Pluto - All-in-One Debug Tool**

### **What is Pluto?**
Pluto is a powerful debug toolkit that provides:
- 🌐 **Network monitoring** - inspect all HTTP requests/responses
- 📋 **Logging** - centralized log viewer
- 💾 **SharedPreferences** - view and edit preferences
- 🗄️ **Database inspector** - browse Room database
- 💥 **Exception tracking** - catch and analyze crashes

### **How to Access Pluto**
1. **Debug FAB**: Tap the debug info button (ℹ️) in the bottom-right corner
2. **Debug Menu**: Tap the "🔍 Debug Tools" option in the toolbar
3. **Notification**: Pluto shows a persistent notification in debug builds

### **Pluto Features Available**

#### **🌐 Network Monitor**
- View all API calls to OpenWeatherMap
- Inspect request/response headers and bodies
- Monitor response times and status codes
- Export network logs

#### **📋 Logger**
```kotlin
// Use PlutoLog instead of Log for better debugging
PlutoLog.d("Navigation", "Settings button clicked")
PlutoLog.e("WeatherAPI", "Failed to fetch weather", exception)
PlutoLog.i("Widget", "Weather widget updated successfully")
```

#### **💾 SharedPreferences Viewer**
- View all stored preferences
- Edit values in real-time
- Useful for testing settings changes

#### **🗄️ Database Inspector**
- Browse Room database tables
- View query results
- Monitor database changes

---

## **🔍 LeakCanary - Memory Leak Detection**

### **What is LeakCanary?**
LeakCanary automatically detects memory leaks in your app and provides detailed analysis.

### **Features**
- ✅ **Automatic detection** - no setup required
- 📊 **Heap analysis** - detailed leak reports
- 🔔 **Notifications** - alerts when leaks are found
- 📱 **Built-in UI** - browse leak reports

### **How It Works**
1. LeakCanary monitors object lifecycles
2. Detects when objects should be garbage collected
3. Triggers heap dumps when leaks are suspected
4. Analyzes heap dumps and shows leak traces

### **Reading Leak Reports**
- **Leak trace**: Shows the reference chain keeping objects alive
- **Culprit**: Highlights the likely cause of the leak
- **Suggestions**: Provides fix recommendations

---

## **🎯 Debug Menu Features**

### **Access Debug Menu**
In debug builds, you'll see a debug menu with:

#### **🔍 Debug Tools**
- Opens Pluto debug panel
- Quick access to all debugging features

#### **📊 Memory Info**
- Shows current memory usage
- Displays available vs used memory
- Helps identify memory pressure

#### **🗑️ Clear Cache**
- Clears app cache directory
- Useful for testing fresh states
- Resets cached data

---

## **📱 Usage in Development**

### **Debugging Network Issues**
1. Open Pluto → Network tab
2. Trigger the problematic API call
3. Inspect request/response details
4. Check for errors or unexpected data

### **Debugging Navigation Issues**
1. Check Pluto → Logger tab
2. Look for navigation-related logs
3. Verify the navigation flow

### **Debugging Settings**
1. Open Pluto → SharedPreferences
2. View current settings values
3. Test by modifying values directly

### **Debugging Memory Issues**
1. Monitor LeakCanary notifications
2. Use Debug Menu → Memory Info
3. Check for unusual memory growth

---

## **🔧 Configuration**

### **Pluto Plugins Enabled**
- ✅ **Network** - HTTP request/response monitoring
- ✅ **Logger** - Centralized logging
- ✅ **Exceptions** - Crash reporting
- ✅ **SharedPreferences** - Settings viewer
- ✅ **Database** - Room database inspector

### **Build Configuration**
```kotlin
// Debug builds only
debugImplementation(libs.pluto)
debugImplementation(libs.pluto.network)
debugImplementation(libs.pluto.logger)
debugImplementation(libs.leakcanary)

// Release builds (no-op)
releaseImplementation(libs.pluto.no.op)
```

### **Network Integration**
```kotlin
// OkHttp interceptor automatically captures all requests
OkHttpClient.Builder()
    .addInterceptor(PlutoInterceptor())
    .build()
```

---

## **🎉 Benefits**

### **🚀 Faster Development**
- Instant network debugging
- Real-time preference editing
- Immediate leak detection

### **🔍 Better Debugging**
- Centralized log viewing
- Visual leak traces
- Network request inspection

### **📊 Performance Insights**
- Memory usage monitoring
- Network performance metrics
- Database query analysis

### **🛡️ Quality Assurance**
- Automatic leak detection
- Exception tracking
- Performance bottleneck identification

---

## **💡 Pro Tips**

### **Network Debugging**
- Use Pluto's network export feature to share API issues
- Filter requests by endpoint or status code
- Compare request/response between different app states

### **Memory Debugging**
- Pay attention to LeakCanary notifications
- Use memory info to identify memory pressure
- Clear cache to test fresh app states

### **Logging Best Practices**
```kotlin
// Use structured logging with PlutoLog
PlutoLog.d("FeatureName", "Action: $action, Result: $result")
PlutoLog.e("FeatureName", "Error occurred", exception)
```

### **Performance Monitoring**
- Monitor network request times in Pluto
- Check memory usage after major operations
- Use database inspector to verify data integrity

---

## **🚫 Production Safety**

### **Automatic Exclusion**
- Pluto and LeakCanary are **automatically excluded** from release builds
- No performance impact in production
- No additional APK size in release

### **Debug-Only Features**
- Debug FAB only visible in debug builds
- Debug menu only appears in debug builds
- All debug code wrapped in `BuildConfig.DEBUG` checks

---

This debug setup provides **comprehensive insights** into your app's behavior during development while maintaining **zero impact** on production builds! 🚀 