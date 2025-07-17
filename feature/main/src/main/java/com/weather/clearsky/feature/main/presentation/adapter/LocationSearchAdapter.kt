package com.weather.clearsky.feature.main.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.weather.clearsky.feature.main.databinding.ItemLocationSearchBinding
import com.weather.clearsky.feature.main.data.model.SavedLocation

/**
 * Adapter for displaying location search results
 */
class LocationSearchAdapter(
    private val onLocationClick: (SavedLocation) -> Unit
) : ListAdapter<SavedLocation, LocationSearchAdapter.LocationViewHolder>(LocationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding = ItemLocationSearchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LocationViewHolder(
        private val binding: ItemLocationSearchBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(location: SavedLocation) {
            with(binding) {
                tvLocationName.text = location.name
                tvLocationCountry.text = if (location.country.isNotEmpty()) {
                    location.country
                } else {
                    "Unknown"
                }

                btnAddLocation.setOnClickListener {
                    onLocationClick(location)
                }

                root.setOnClickListener {
                    onLocationClick(location)
                }
            }
        }
    }

    private class LocationDiffCallback : DiffUtil.ItemCallback<SavedLocation>() {
        override fun areItemsTheSame(oldItem: SavedLocation, newItem: SavedLocation): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SavedLocation, newItem: SavedLocation): Boolean {
            return oldItem == newItem
        }
    }
} 