package com.weather.clearsky

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.weather.clearsky.core.common.navigation.NavigationCallback
import com.weather.clearsky.databinding.ActivityMainBinding
import com.weather.clearsky.feature.main.presentation.adapter.LocationPagerAdapter
import com.weather.clearsky.feature.main.presentation.viewmodel.LocationsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var locationPagerAdapter: LocationPagerAdapter
    private val locationsViewModel: LocationsViewModel by viewModels()
    private var locationDots = mutableListOf<TextView>()
    private var currentAnimators = mutableListOf<ValueAnimator>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        observeViewModel()
        
        // Initialize locations
        locationsViewModel.initializeLocations()
    }

    private fun setupViewPager() {
        locationPagerAdapter = LocationPagerAdapter(this)
        
        binding.viewPagerLocations.apply {
            adapter = locationPagerAdapter
            
            // Enable smooth page change callbacks with animation
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                    animateDotsSmooth(position, positionOffset)
                }
                
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    // Final state when page is fully selected
                    updateLocationIndicators(position)
                }
            })
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            locationsViewModel.savedLocations.collect { locations ->
                locationPagerAdapter.updateLocations(locations)
                updateLocationDots(locations.size + 1) // +1 for add location tab
            }
        }
    }

    private fun updateLocationDots(totalCount: Int) {
        // Clear existing dots and animations
        currentAnimators.forEach { it.cancel() }
        currentAnimators.clear()
        binding.locationDots.removeAllViews()
        locationDots.clear()

        val primaryColor = ContextCompat.getColor(this, R.color.primary_color)
        val inactiveColor = ContextCompat.getColor(this, android.R.color.darker_gray)

        for (i in 0 until totalCount) {
            val dot = TextView(this).apply {
                text = if (i < totalCount - 1) "●" else "+"
                textSize = 16f
                setTextColor(if (i == 0) primaryColor else inactiveColor)
                setPadding(12, 8, 12, 8)
                alpha = if (i == 0) 1f else 0.6f
                scaleX = if (i == 0) 1.2f else 1f
                scaleY = if (i == 0) 1.2f else 1f
                
                // Add text shadow for better visibility against any background
                setShadowLayer(4f, 0f, 2f, android.graphics.Color.BLACK)
            }
            
            locationDots.add(dot)
            binding.locationDots.addView(dot)
        }
    }

    private fun animateDotsSmooth(currentPosition: Int, positionOffset: Float) {
        if (locationDots.isEmpty()) return
        
        val primaryColor = ContextCompat.getColor(this, R.color.primary_color)
        val inactiveColor = ContextCompat.getColor(this, android.R.color.darker_gray)
        
        locationDots.forEachIndexed { index, dot ->
            when {
                // Current page - animate from selected to normal
                index == currentPosition -> {
                    val progress = positionOffset
                    
                    // Scale animation: from 1.2 to 1.0
                    val scale = 1.2f - (0.2f * progress)
                    dot.scaleX = scale
                    dot.scaleY = scale
                    
                    // Alpha animation: from 1.0 to 0.6
                    dot.alpha = 1f - (0.4f * progress)
                    
                    // Color transition (if needed, can be added)
                    dot.setTextColor(primaryColor)
                }
                
                // Next page - animate from normal to selected
                index == currentPosition + 1 -> {
                    val progress = positionOffset
                    
                    // Scale animation: from 1.0 to 1.2
                    val scale = 1f + (0.2f * progress)
                    dot.scaleX = scale
                    dot.scaleY = scale
                    
                    // Alpha animation: from 0.6 to 1.0
                    dot.alpha = 0.6f + (0.4f * progress)
                    
                    // Color transition
                    if (progress > 0.5f) {
                        dot.setTextColor(primaryColor)
                    } else {
                        dot.setTextColor(inactiveColor)
                    }
                }
                
                // All other dots - inactive state
                else -> {
                    dot.scaleX = 1f
                    dot.scaleY = 1f
                    dot.alpha = 0.6f
                    dot.setTextColor(inactiveColor)
                }
            }
        }
    }

    private fun updateLocationIndicators(currentPosition: Int) {
        val primaryColor = ContextCompat.getColor(this, R.color.primary_color)
        val inactiveColor = ContextCompat.getColor(this, android.R.color.darker_gray)
        
        locationDots.forEachIndexed { index, dot ->
            val isSelected = index == currentPosition
            
            // Animate to final state smoothly
            val targetScale = if (isSelected) 1.2f else 1f
            val targetAlpha = if (isSelected) 1f else 0.6f
            val targetColor = if (isSelected) primaryColor else inactiveColor
            
            // Scale animation
            dot.animate()
                .scaleX(targetScale)
                .scaleY(targetScale)
                .alpha(targetAlpha)
                .setDuration(200)
                .start()
            
            dot.setTextColor(targetColor)
        }
    }

    override fun navigateToSettings() {
        // Navigate to settings (can be implemented later)
    }

    override fun navigateToMain() {
        // Navigate to main location (first page)
        binding.viewPagerLocations.currentItem = 0
    }

    override fun navigateToAlerts() {
        // Navigate to alerts (can be implemented later)  
    }

    override fun navigateToLocation(locationId: String) {
        // Find the position of the location in the adapter
        val locations = locationsViewModel.getSavedLocations()
        val position = locations.indexOfFirst { it.id == locationId }
        
        if (position != -1) {
            // Navigate to the specific location page
            binding.viewPagerLocations.currentItem = position
        } else {
            // If location not found, navigate to main (first location)
            binding.viewPagerLocations.currentItem = 0
        }
    }

    override fun navigateToLocationManager() {
        // Navigate to location manager using fragment transaction
        val locationManagerFragment = com.weather.clearsky.feature.main.presentation.fragment.LocationManagerFragment()
        
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, locationManagerFragment)
            .addToBackStack("location_manager")
            .commit()
    }

    override fun onBackPressed() {
        // Handle back press for ViewPager2
        if (binding.viewPagerLocations.currentItem == 0) {
            super.onBackPressed()
        } else {
            // Go back to first page (main location)
            binding.viewPagerLocations.currentItem = 0
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel any running animations
        currentAnimators.forEach { it.cancel() }
        currentAnimators.clear()
    }
} 