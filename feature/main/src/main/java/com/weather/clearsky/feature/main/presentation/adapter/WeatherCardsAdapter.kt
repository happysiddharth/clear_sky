package com.weather.clearsky.feature.main.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.weather.clearsky.feature.main.databinding.*
import com.weather.clearsky.feature.main.presentation.model.WeatherCard
import com.weather.clearsky.feature.main.presentation.model.CardType
import java.util.*

/**
 * Adapter for weather cards with drag & drop support
 */
class WeatherCardsAdapter(
    private val onCardClick: (WeatherCard) -> Unit = {},
    private val onCardMoved: (List<WeatherCard>) -> Unit = {}
) : ListAdapter<WeatherCard, RecyclerView.ViewHolder>(WeatherCardDiffCallback()) {

    companion object {
        private const val TYPE_TEMPERATURE = 0
        private const val TYPE_AIR_QUALITY = 1
        private const val TYPE_HUMIDITY = 2
        private const val TYPE_WIND = 3
        private const val TYPE_UV_INDEX = 4
        private const val TYPE_FORECAST = 5
        private const val TYPE_HOURLY_WEATHER = 6
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).cardType) {
            CardType.TEMPERATURE -> TYPE_TEMPERATURE
            CardType.AIR_QUALITY -> TYPE_AIR_QUALITY
            CardType.HUMIDITY -> TYPE_HUMIDITY
            CardType.WIND -> TYPE_WIND
            CardType.UV_INDEX -> TYPE_UV_INDEX
            CardType.FORECAST -> TYPE_FORECAST
            CardType.HOURLY_WEATHER -> TYPE_HOURLY_WEATHER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        android.util.Log.d("WeatherCardsAdapter", "Creating ViewHolder for viewType: $viewType")
        return when (viewType) {
            TYPE_TEMPERATURE -> TemperatureCardViewHolder(
                CardTemperatureBinding.inflate(inflater, parent, false)
            )
            TYPE_AIR_QUALITY -> AirQualityCardViewHolder(
                CardAirQualityBinding.inflate(inflater, parent, false)
            )
            TYPE_HUMIDITY -> HumidityCardViewHolder(
                CardHumidityBinding.inflate(inflater, parent, false)
            )
            TYPE_WIND -> WindCardViewHolder(
                CardWindBinding.inflate(inflater, parent, false)
            )
            TYPE_UV_INDEX -> UvIndexCardViewHolder(
                CardUvIndexBinding.inflate(inflater, parent, false)
            )
            TYPE_FORECAST -> ForecastCardViewHolder(
                CardForecastBinding.inflate(inflater, parent, false)
            )
            TYPE_HOURLY_WEATHER -> {
                android.util.Log.d("WeatherCardsAdapter", "Creating HourlyWeatherCardViewHolder")
                HourlyWeatherCardViewHolder(
                    CardHourlyWeatherBinding.inflate(inflater, parent, false)
                )
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val card = getItem(position)
        when (holder) {
            is TemperatureCardViewHolder -> holder.bind(card as WeatherCard.TemperatureCard)
            is AirQualityCardViewHolder -> holder.bind(card as WeatherCard.AirQualityCard)
            is HumidityCardViewHolder -> holder.bind(card as WeatherCard.HumidityCard)
            is WindCardViewHolder -> holder.bind(card as WeatherCard.WindCard)
            is UvIndexCardViewHolder -> holder.bind(card as WeatherCard.UvIndexCard)
            is ForecastCardViewHolder -> holder.bind(card as WeatherCard.ForecastCard)
            is HourlyWeatherCardViewHolder -> holder.bind(card as WeatherCard.HourlyWeatherCard)
        }
    }

    /**
     * Move item for drag & drop functionality
     */
    fun moveItem(fromPosition: Int, toPosition: Int) {
        val currentList = currentList.toMutableList()
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(currentList, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(currentList, i, i - 1)
            }
        }
        
        // Update positions
        currentList.forEachIndexed { index, card ->
            card.position = index
        }
        
        submitList(currentList)
        onCardMoved(currentList)
    }

    /**
     * ViewHolder for Temperature Card
     */
    inner class TemperatureCardViewHolder(
        private val binding: CardTemperatureBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(card: WeatherCard.TemperatureCard) {
            binding.apply {
                tvTemperature.text = card.temperature
                tvFeelsLike.text = "Feels like ${card.feelsLike}"
                tvLocation.text = card.location
                tvDescription.text = card.description
                tvWeatherIcon.text = card.icon
                
                root.setOnClickListener { onCardClick(card) }
            }
        }
    }

    /**
     * ViewHolder for Air Quality Card
     */
    inner class AirQualityCardViewHolder(
        private val binding: CardAirQualityBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(card: WeatherCard.AirQualityCard) {
            binding.apply {
                tvAqi.text = card.aqi.toString()
                tvQuality.text = card.quality
                tvPm25.text = "PM2.5: ${card.pm25}"
                tvPm10.text = "PM10: ${card.pm10}"
                tvCo.text = "CO: ${card.co}"
                tvNo2.text = "NO2: ${card.no2}"
                
                // Set AQI color based on value
                val color = when {
                    card.aqi <= 50 -> android.graphics.Color.GREEN
                    card.aqi <= 100 -> android.graphics.Color.YELLOW
                    card.aqi <= 150 -> android.graphics.Color.parseColor("#FF8C00")
                    card.aqi <= 200 -> android.graphics.Color.RED
                    card.aqi <= 300 -> android.graphics.Color.parseColor("#8B00FF")
                    else -> android.graphics.Color.parseColor("#7E0023")
                }
                tvAqi.setTextColor(color)
                
                root.setOnClickListener { onCardClick(card) }
            }
        }
    }

    /**
     * ViewHolder for Humidity Card
     */
    inner class HumidityCardViewHolder(
        private val binding: CardHumidityBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(card: WeatherCard.HumidityCard) {
            binding.apply {
                tvHumidity.text = card.humidity
                tvDewPoint.text = "Dew Point: ${card.dewPoint}"
                tvPressure.text = "Pressure: ${card.pressure}"
                
                root.setOnClickListener { onCardClick(card) }
            }
        }
    }

    /**
     * ViewHolder for Wind Card
     */
    inner class WindCardViewHolder(
        private val binding: CardWindBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(card: WeatherCard.WindCard) {
            binding.apply {
                tvWindSpeed.text = card.windSpeed
                tvWindDirection.text = "Direction: ${card.windDirection}"
                tvWindGust.text = "Gusts: ${card.windGust}"
                tvVisibility.text = "Visibility: ${card.visibility}"
                
                root.setOnClickListener { onCardClick(card) }
            }
        }
    }

    /**
     * ViewHolder for UV Index Card
     */
    inner class UvIndexCardViewHolder(
        private val binding: CardUvIndexBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(card: WeatherCard.UvIndexCard) {
            binding.apply {
                tvUvIndex.text = card.uvIndex.toString()
                tvUvLevel.text = card.uvLevel
                tvUvDescription.text = card.uvDescription
                tvSunrise.text = "Sunrise: ${card.sunriseTime}"
                tvSunset.text = "Sunset: ${card.sunsetTime}"
                
                // Set UV index color
                val color = when {
                    card.uvIndex <= 2 -> android.graphics.Color.GREEN
                    card.uvIndex <= 5 -> android.graphics.Color.YELLOW
                    card.uvIndex <= 7 -> android.graphics.Color.parseColor("#FF8C00")
                    card.uvIndex <= 10 -> android.graphics.Color.RED
                    else -> android.graphics.Color.parseColor("#8B00FF")
                }
                tvUvIndex.setTextColor(color)
                
                root.setOnClickListener { onCardClick(card) }
            }
        }
    }

    /**
     * ViewHolder for Forecast Card
     */
    inner class ForecastCardViewHolder(
        private val binding: CardForecastBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(card: WeatherCard.ForecastCard) {
            binding.apply {
                tvTodayHigh.text = "H: ${card.todayHigh}"
                tvTodayLow.text = "L: ${card.todayLow}"
                tvTomorrowHigh.text = "H: ${card.tomorrowHigh}"
                tvTomorrowLow.text = "L: ${card.tomorrowLow}"
                
                root.setOnClickListener { onCardClick(card) }
            }
        }
    }

    inner class HourlyWeatherCardViewHolder(
        private val binding: CardHourlyWeatherBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val hourlyForecastAdapter = HourlyForecastAdapter()

        init {
            binding.rvHourlyForecast.apply {
                adapter = hourlyForecastAdapter
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                    context, 
                    androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, 
                    false
                )
            }
        }

        fun bind(card: WeatherCard.HourlyWeatherCard) {
            with(binding) {
                tvCurrentTime.text = card.currentHour
                tvLocation.text = card.location
                
                // Submit hourly forecast data to the nested RecyclerView
                hourlyForecastAdapter.submitList(card.hourlyForecast)
                
                root.setOnClickListener { onCardClick(card) }
            }
        }
    }
}

/**
 * DiffUtil callback for efficient list updates
 */
class WeatherCardDiffCallback : DiffUtil.ItemCallback<WeatherCard>() {
    override fun areItemsTheSame(oldItem: WeatherCard, newItem: WeatherCard): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: WeatherCard, newItem: WeatherCard): Boolean {
        return oldItem == newItem
    }
} 