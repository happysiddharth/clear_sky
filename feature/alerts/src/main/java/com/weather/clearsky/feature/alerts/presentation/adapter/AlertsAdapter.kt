package com.weather.clearsky.feature.alerts.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.weather.clearsky.feature.alerts.databinding.ItemWeatherAlertBinding
import com.weather.clearsky.feature.alerts.domain.entity.*
import java.time.format.DateTimeFormatter
import java.util.*

class AlertsAdapter(
    private val onEditClick: (WeatherAlert) -> Unit,
    private val onDeleteClick: (WeatherAlert) -> Unit,
    private val onStatusToggle: (WeatherAlert) -> Unit
) : ListAdapter<WeatherAlert, AlertsAdapter.AlertViewHolder>(AlertDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val binding = ItemWeatherAlertBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AlertViewHolder(
        private val binding: ItemWeatherAlertBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(alert: WeatherAlert) {
            with(binding) {
                // Set alert icon based on type
                tvAlertIcon.text = alert.alertType.icon
                
                // Set alert title
                tvAlertTitle.text = alert.title
                
                // Set location
                tvAlertLocation.text = "📍 ${alert.location.displayName}"
                
                // Set condition description
                tvAlertCondition.text = formatCondition(alert.condition)
                
                // Set target date/time
                tvTargetDatetime.text = formatDateTime(alert.targetDateTime)
                
                // Set status badge
                tvStatusBadge.text = alert.status.displayName.uppercase(Locale.getDefault())
                tvStatusBadge.setBackgroundResource(getStatusColor(alert.status))
                
                // Set active switch
                switchActive.isChecked = alert.status == AlertStatus.ACTIVE
                switchActive.setOnCheckedChangeListener { _, _ ->
                    onStatusToggle(alert)
                }
                
                // Show last triggered if applicable
                if (alert.lastTriggeredAt != null) {
                    tvLastTriggered.visibility = android.view.View.VISIBLE
                    tvLastTriggered.text = "⚡ Last triggered: ${formatDateTime(alert.lastTriggeredAt!!)}"
                } else {
                    tvLastTriggered.visibility = android.view.View.GONE
                }
                
                // Set click listeners
                btnEdit.setOnClickListener { onEditClick(alert) }
                btnDelete.setOnClickListener { onDeleteClick(alert) }
            }
        }
        
        private fun formatCondition(condition: AlertCondition): String {
            return when (condition) {
                is AlertCondition.TemperatureCondition -> {
                    "When temperature ${condition.operator.symbol} ${condition.value}${condition.unit.symbol}"
                }
                is AlertCondition.RainCondition -> {
                    "When rain ${condition.operator.symbol} ${condition.value}mm/h"
                }
                is AlertCondition.SnowCondition -> {
                    "When snow ${condition.operator.symbol} ${condition.value}mm/h"
                }
                is AlertCondition.WindCondition -> {
                    "When wind speed ${condition.operator.symbol} ${condition.value}km/h"
                }
                is AlertCondition.HumidityCondition -> {
                    "When humidity ${condition.operator.symbol} ${condition.value}%"
                }
                is AlertCondition.WeatherCondition -> {
                    "When weather is ${condition.weatherType.displayName}"
                }
                is AlertCondition.UvIndexCondition -> {
                    "When UV index ${condition.operator.symbol} ${condition.value}"
                }
                is AlertCondition.PressureCondition -> {
                    "When pressure ${condition.operator.symbol} ${condition.value}hPa"
                }
                is AlertCondition.VisibilityCondition -> {
                    "When visibility ${condition.operator.symbol} ${condition.value}km"
                }
            }
        }
        
        private fun formatDateTime(dateTime: java.time.LocalDateTime): String {
            val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' h:mm a")
            return dateTime.format(formatter)
        }
        
        private fun getStatusColor(status: AlertStatus): Int {
            return when (status) {
                AlertStatus.ACTIVE -> android.R.color.holo_green_dark
                AlertStatus.INACTIVE -> android.R.color.darker_gray
                AlertStatus.TRIGGERED -> android.R.color.holo_orange_dark
                AlertStatus.EXPIRED -> android.R.color.holo_red_dark
                AlertStatus.CANCELLED -> android.R.color.darker_gray
            }
        }
    }
    
    private class AlertDiffCallback : DiffUtil.ItemCallback<WeatherAlert>() {
        override fun areItemsTheSame(oldItem: WeatherAlert, newItem: WeatherAlert): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WeatherAlert, newItem: WeatherAlert): Boolean {
            return oldItem == newItem
        }
    }
} 