package com.weather.clearsky.feature.main.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.weather.clearsky.feature.main.databinding.ItemLocationManagerBinding
import com.weather.clearsky.feature.main.data.model.SavedLocation
import android.view.View

/**
 * Adapter for managing saved locations with reorder capabilities
 */
class LocationManagerAdapter(
    private val onMoveUp: (SavedLocation) -> Unit,
    private val onMoveDown: (SavedLocation) -> Unit,
    private val onDelete: (SavedLocation) -> Unit,
    private val onLocationClick: (SavedLocation) -> Unit
) : ListAdapter<SavedLocation, LocationManagerAdapter.LocationViewHolder>(LocationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding = ItemLocationManagerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class LocationViewHolder(
        private val binding: ItemLocationManagerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(location: SavedLocation, position: Int) {
            with(binding) {
                // Location details
                tvLocationName.text = location.name
                tvLocationDetails.text = if (location.country.isNotEmpty()) {
                    if (location.area.isNotEmpty() && location.isCurrentLocation) {
                        "${location.area}, ${location.country}"
                    } else {
                        location.country
                    }
                } else {
                    "Unknown location"
                }

                // Current location indicator
                tvCurrentIndicator.visibility = if (location.isCurrentLocation) View.VISIBLE else View.GONE

                // Control button states
                btnMoveUp.isEnabled = position > 0
                btnMoveDown.isEnabled = position < itemCount - 1
                btnMoveUp.alpha = if (position > 0) 1.0f else 0.3f
                btnMoveDown.alpha = if (position < itemCount - 1) 1.0f else 0.3f

                // Click listeners
                btnMoveUp.setOnClickListener {
                    if (position > 0) {
                        onMoveUp(location)
                    }
                }

                btnMoveDown.setOnClickListener {
                    if (position < itemCount - 1) {
                        onMoveDown(location)
                    }
                }

                btnDelete.setOnClickListener {
                    onDelete(location)
                }

                root.setOnClickListener {
                    onLocationClick(location)
                }
            }
        }
    }

    /**
     * Move item from one position to another
     */
    fun moveItem(fromPosition: Int, toPosition: Int) {
        val currentList = currentList.toMutableList()
        if (fromPosition < currentList.size && toPosition < currentList.size) {
            val item = currentList.removeAt(fromPosition)
            currentList.add(toPosition, item)
            submitList(currentList)
        }
    }

    /**
     * Get current order of items
     */
    fun getCurrentOrder(): List<SavedLocation> {
        return currentList.toList()
    }
}

/**
 * DiffUtil callback for location list updates
 */
class LocationDiffCallback : DiffUtil.ItemCallback<SavedLocation>() {
    override fun areItemsTheSame(oldItem: SavedLocation, newItem: SavedLocation): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SavedLocation, newItem: SavedLocation): Boolean {
        return oldItem == newItem
    }
} 