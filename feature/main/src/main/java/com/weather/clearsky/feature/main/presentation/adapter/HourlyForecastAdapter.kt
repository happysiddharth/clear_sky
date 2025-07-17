package com.weather.clearsky.feature.main.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.weather.clearsky.feature.main.databinding.ItemHourlyForecastBinding
import com.weather.clearsky.feature.main.presentation.model.HourlyForecast

class HourlyForecastAdapter : ListAdapter<HourlyForecast, HourlyForecastAdapter.HourlyViewHolder>(
    HourlyForecastDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyViewHolder {
        val binding = ItemHourlyForecastBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HourlyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HourlyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HourlyViewHolder(
        private val binding: ItemHourlyForecastBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(hourlyForecast: HourlyForecast) {
            with(binding) {
                tvHourTime.text = hourlyForecast.time
                tvHourIcon.text = hourlyForecast.icon
                tvHourTemperature.text = hourlyForecast.temperature
                
                // Show precipitation chance if > 0
                if (hourlyForecast.precipitationChance > 0) {
                    tvPrecipitationChance.text = "${hourlyForecast.precipitationChance}%"
                    tvPrecipitationChance.visibility = View.VISIBLE
                } else {
                    tvPrecipitationChance.visibility = View.GONE
                }
            }
        }
    }

    private class HourlyForecastDiffCallback : DiffUtil.ItemCallback<HourlyForecast>() {
        override fun areItemsTheSame(oldItem: HourlyForecast, newItem: HourlyForecast): Boolean {
            return oldItem.time == newItem.time
        }

        override fun areContentsTheSame(oldItem: HourlyForecast, newItem: HourlyForecast): Boolean {
            return oldItem == newItem
        }
    }
} 