package com.weather.clearsky.feature.main.presentation.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.weather.clearsky.feature.main.data.model.SavedLocation
import com.weather.clearsky.feature.main.presentation.fragment.MainFragment
import com.weather.clearsky.feature.main.presentation.fragment.AddLocationFragment

/**
 * ViewPager2 adapter for swiping between different weather locations
 * Enhanced to support dynamic location reordering
 */
class LocationPagerAdapter(
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {

    private var locations = listOf<SavedLocation>()
    private var showAddLocationTab = true

    fun updateLocations(newLocations: List<SavedLocation>) {
        val oldLocations = locations
        locations = newLocations.sortedBy { it.order } // Ensure proper ordering
        
        // Only notify if there are actual changes
        if (oldLocations != locations) {
            notifyDataSetChanged()
        }
    }

    fun setShowAddLocationTab(show: Boolean) {
        if (showAddLocationTab != show) {
            showAddLocationTab = show
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return locations.size + if (showAddLocationTab) 1 else 0
    }

    override fun createFragment(position: Int): Fragment {
        return if (position < locations.size) {
            // Weather fragment for a specific location
            val location = locations[position]
            MainFragment().apply {
                arguments = Bundle().apply {
                    putString("location_id", location.id)
                    putString("location_name", location.name)
                    putString("location_country", location.country)
                    putString("location_area", location.area)
                    putDouble("location_latitude", location.latitude)
                    putDouble("location_longitude", location.longitude)
                    putBoolean("is_current_location", location.isCurrentLocation)
                    putInt("location_order", location.order)
                }
            }
        } else {
            // Add location fragment
            AddLocationFragment()
        }
    }

    override fun getItemId(position: Int): Long {
        return if (position < locations.size) {
            // Use a combination of ID and order to ensure proper updates
            "${locations[position].id}_${locations[position].order}".hashCode().toLong()
        } else {
            "add_location".hashCode().toLong()
        }
    }

    override fun containsItem(itemId: Long): Boolean {
        return locations.any { "${it.id}_${it.order}".hashCode().toLong() == itemId } ||
                itemId == "add_location".hashCode().toLong()
    }

    fun getLocationAt(position: Int): SavedLocation? {
        return if (position < locations.size) locations[position] else null
    }

    fun isAddLocationTab(position: Int): Boolean {
        return position >= locations.size
    }

    /**
     * Get the current position of a location by its ID
     */
    fun getPositionByLocationId(locationId: String): Int {
        return locations.indexOfFirst { it.id == locationId }
    }

    /**
     * Get all current locations in their display order
     */
    fun getAllLocations(): List<SavedLocation> {
        return locations.toList()
    }

    /**
     * Check if the adapter has any locations
     */
    fun hasLocations(): Boolean {
        return locations.isNotEmpty()
    }
} 