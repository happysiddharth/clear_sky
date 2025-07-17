package com.weather.clearsky.feature.main.presentation.fragment

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.MenuItem
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.weather.clearsky.core.common.navigation.NavigationCallback
import com.weather.clearsky.core.common.navigation.NavigationManager
import com.weather.clearsky.feature.main.databinding.FragmentMainBinding
import com.weather.clearsky.feature.main.R
import com.weather.clearsky.feature.main.presentation.adapter.WeatherCardTouchHelper
import com.weather.clearsky.feature.main.presentation.adapter.WeatherCardsAdapter
import com.weather.clearsky.feature.main.presentation.model.WeatherCard
import com.weather.clearsky.feature.main.presentation.model.WeatherCardUiState
import com.weather.clearsky.feature.main.presentation.util.WeatherVideoMapper
import com.weather.clearsky.feature.main.presentation.viewmodel.MainViewModel
import com.weather.clearsky.feature.weather.presentation.widget.WeatherWidgetProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import android.content.pm.PackageManager

@AndroidEntryPoint
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels()
    
    @Inject
    lateinit var navigationManager: NavigationManager
    
    private lateinit var cardsAdapter: WeatherCardsAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var videoBackground: VideoView

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) {
            Toast.makeText(requireContext(), "Location permission granted!", Toast.LENGTH_SHORT).show()
            viewModel.checkPermissions()
            viewModel.refreshWeatherData()
        } else {
            Toast.makeText(
                requireContext(),
                "Location permission denied. Widget will use default location.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Read location arguments if available
        val locationData = arguments?.let { args ->
            if (args.containsKey("location_id")) {
                LocationArguments(
                    id = args.getString("location_id", ""),
                    name = args.getString("location_name", ""),
                    country = args.getString("location_country", ""),
                    area = args.getString("location_area", ""),
                    latitude = args.getDouble("location_latitude", 0.0),
                    longitude = args.getDouble("location_longitude", 0.0),
                    isCurrentLocation = args.getBoolean("is_current_location", false)
                )
            } else null
        }
        
        // Set "MY LOCATION" text and area visibility based on location type
        updateLocationTextVisibility(locationData)
        
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        setupVideoBackground()
        observeViewModel()
        viewModel.checkPermissions()
        
        // Initialize with specific location data if available
        if (locationData != null) {
            viewModel.setLocationData(locationData)
        }
        
        initializeHeroWeatherData()
    }



    private fun navigateToLocationManager() {
        // Navigate to the location manager using the navigation callback
        (activity as? NavigationCallback)?.navigateToLocationManager()
    }

    // Data class for location arguments
    data class LocationArguments(
        val id: String,
        val name: String,
        val country: String,
        val area: String,
        val latitude: Double,
        val longitude: Double,
        val isCurrentLocation: Boolean
    )

    private fun setupToolbar() {
        // Set up menu item click listener for the toolbar
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_manage_locations -> {
                    navigateToLocationManager()
                    true
                }
                R.id.action_create_alert -> {
                    Toast.makeText(requireContext(), "Create Alert clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_settings -> {
                    (activity as? NavigationCallback)?.navigateToSettings()
                    true
                }
                else -> false
            }
        }
        
        // Setup collapse listener to control collapsed weather info visibility
        binding.collapsingToolbar.setOnApplyWindowInsetsListener { _, insets ->
            insets
        }
        
        // Add offset change listener to track collapse state
        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange
            val percentage = Math.abs(verticalOffset).toFloat() / totalScrollRange.toFloat()
            
            // Control hero content visibility - fade out as it collapses
            if (percentage < 0.7f) {
                // Hero fully visible when not much collapsed
                binding.heroWeatherContainer.alpha = 1f
                binding.heroWeatherContainer.visibility = View.VISIBLE
            } else if (percentage < 0.9f) {
                // Fade out hero content as it collapses
                val fadeAlpha = 1f - ((percentage - 0.7f) / 0.2f)
                binding.heroWeatherContainer.alpha = fadeAlpha
                binding.heroWeatherContainer.visibility = View.VISIBLE
            } else {
                // Hide hero completely when fully collapsed
                binding.heroWeatherContainer.alpha = 0f
                binding.heroWeatherContainer.visibility = View.INVISIBLE
            }
            
            // Show collapsed weather info when toolbar is mostly collapsed (>80%)
            if (percentage > 0.8f) {
                binding.collapsedWeatherInfo.visibility = View.VISIBLE
                binding.collapsedWeatherInfo.alpha = (percentage - 0.8f) / 0.2f
            } else {
                binding.collapsedWeatherInfo.visibility = View.GONE
                binding.collapsedWeatherInfo.alpha = 0f
            }
        }
    }



    private fun initializeHeroWeatherData() {
        // Set initial placeholder data
        updateHeroWeatherSection(
            location = "Loading...",
            temperature = "--°",
            condition = "Loading...",
            high = "--°",
            low = "--°"
        )
    }
    
    private fun setupVideoBackground() {
        videoBackground = binding.videoWeatherBackground
        
        // Set up video properties
        videoBackground.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = true
            mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
            
            // Mute the video (we only want visual background)
            mediaPlayer.setVolume(0f, 0f)
            
            // Configure audio attributes to not interfere with other audio
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
                mediaPlayer.setAudioAttributes(audioAttributes)
            } else {
                // For older versions, use a stream type that's less likely to interfere
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
            }
            
            // Start playing
            videoBackground.start()
            
            // Explicitly abandon audio focus to allow background audio to continue
            abandonAudioFocus()
        }
        
        videoBackground.setOnErrorListener { _, what, extra ->
            // Handle video playback errors gracefully
            // Fall back to gradient background
            setFallbackBackground()
            false
        }
        
        // Set default video
        setWeatherVideo("default")
    }
    
    private fun setWeatherVideo(condition: String) {
        try {
            val videoResource = WeatherVideoMapper.getWeatherVideo(condition)
            val uri = Uri.parse("android.resource://${requireContext().packageName}/$videoResource")
            
            videoBackground.setVideoURI(uri)
            videoBackground.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.isLooping = true
                mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
                mediaPlayer.setVolume(0f, 0f)
                
                // Configure audio attributes to not interfere with other audio
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    val audioAttributes = AudioAttributes.Builder()
//                        .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
//                        .setUsage(AudioAttributes.USAGE_MEDIA)
//                        .build()
//                    mediaPlayer.setAudioAttributes(audioAttributes)
//                } else {
//                    // For older versions, use a stream type that's less likely to interfere
//                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
//                }
                
                videoBackground.start()
                
                // Explicitly abandon audio focus to allow background audio to continue
                abandonAudioFocus()
            }
        } catch (e: Exception) {
            // Fallback to gradient background if video fails
            setFallbackBackground()
        }
    }
    
    private fun abandonAudioFocus() {
        try {
            val audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.abandonAudioFocus(null)
        } catch (e: Exception) {
            // Ignore any errors - this is a best effort to release audio focus
        }
    }
    
    private fun setFallbackBackground() {
        // Hide video and show gradient background on hero container
        videoBackground.visibility = View.GONE
        binding.heroWeatherContainer.setBackgroundResource(com.weather.clearsky.feature.main.R.drawable.weather_hero_gradient)
    }

    private fun setupRecyclerView() {
        cardsAdapter = WeatherCardsAdapter(
            onCardClick = { card ->
                onCardClick(card)
            },
            onCardMoved = { reorderedCards ->
                viewModel.onCardMoved(reorderedCards)
            }
        )

        binding.rvWeatherCards.apply {
            adapter = cardsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            
            // Setup drag & drop
            val touchHelper = WeatherCardTouchHelper(cardsAdapter)
            itemTouchHelper = ItemTouchHelper(touchHelper)
            itemTouchHelper.attachToRecyclerView(this)
        }
    }

    private fun setupClickListeners() {
        // Comment out missing UI elements - these views don't exist in the current layout
        /*
        binding.btnRequestPermission.setOnClickListener {
            requestLocationPermissions()
        }
        
        binding.btnAddWidget.setOnClickListener {
            openWidgetSelector()
        }
        
        // Add long press to test widget update
        binding.btnAddWidget.setOnLongClickListener {
            testWidgetUpdate()
            true
        }
        */

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshWeatherData()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { uiState ->
                        updateUI(uiState)
                    }
                }
                
                launch {
                    viewModel.permissionStatus.collect { permissionStatus ->
                        updatePermissionStatus(permissionStatus)
                    }
                }

                launch {
                    viewModel.heroWeatherData.collect { heroData ->
                        heroData?.let { data ->
                            updateHeroWeatherSection(
                                location = data.location,
                                temperature = data.temperature,
                                condition = data.condition,
                                high = data.high,
                                low = data.low
                            )
                        }
                    }
                }
            }
        }
    }

    private fun updateHeroWeatherSection(
        location: String,
        temperature: String,
        condition: String,
        high: String,
        low: String
    ) {
        // Update expanded hero section
        binding.tvLocationName.text = location
        binding.tvMainTemperature.text = temperature
        binding.tvWeatherCondition.text = condition
        binding.tvHighLow.text = "H:$high L:$low"
        
        // Update simplified collapsed weather info (only location and temperature)
        binding.tvCollapsedLocation.text = location
        binding.tvCollapsedTemperature.text = temperature
        
        // Dynamically adjust layout based on content length
        adjustHeroLayoutForContent()
        
        // Update video background based on weather condition
        if (::videoBackground.isInitialized && condition != "Loading...") {
            setWeatherVideo(condition)
        }
    }

    /**
     * Dynamically adjust hero layout based on content length
     */
    private fun adjustHeroLayoutForContent() {
        // Adjust location name text size based on length
        val locationText = binding.tvLocationName.text.toString()
        when {
            locationText.length > 20 -> {
                // Very long location names
                binding.tvLocationName.setAutoSizeTextTypeUniformWithConfiguration(
                    20, 36, 2, android.util.TypedValue.COMPLEX_UNIT_SP
                )
                binding.tvLocationName.maxLines = 3
            }
            locationText.length > 12 -> {
                // Medium length location names
                binding.tvLocationName.setAutoSizeTextTypeUniformWithConfiguration(
                    28, 42, 2, android.util.TypedValue.COMPLEX_UNIT_SP
                )
                binding.tvLocationName.maxLines = 2
            }
            else -> {
                // Short location names
                binding.tvLocationName.setAutoSizeTextTypeUniformWithConfiguration(
                    36, 42, 2, android.util.TypedValue.COMPLEX_UNIT_SP
                )
                binding.tvLocationName.maxLines = 1
            }
        }

        // Adjust area name based on length
        val areaText = binding.tvAreaName.text.toString()
        if (areaText.length > 15) {
            binding.tvAreaName.setAutoSizeTextTypeUniformWithConfiguration(
                12, 16, 1, android.util.TypedValue.COMPLEX_UNIT_SP
            )
        } else {
            binding.tvAreaName.setAutoSizeTextTypeUniformWithConfiguration(
                14, 18, 1, android.util.TypedValue.COMPLEX_UNIT_SP
            )
        }

        // Adjust weather condition text based on length
        val conditionText = binding.tvWeatherCondition.text.toString()
        if (conditionText.length > 15) {
            binding.tvWeatherCondition.setAutoSizeTextTypeUniformWithConfiguration(
                14, 18, 1, android.util.TypedValue.COMPLEX_UNIT_SP
            )
        } else {
            binding.tvWeatherCondition.setAutoSizeTextTypeUniformWithConfiguration(
                16, 20, 1, android.util.TypedValue.COMPLEX_UNIT_SP
            )
        }
    }

    private fun updateLocationTextVisibility(locationData: LocationArguments?) {
        val isCurrentLocation = locationData?.isCurrentLocation ?: true
        
        // Show/hide "MY LOCATION" label
        binding.tvMyLocationLabel.visibility = if (isCurrentLocation) View.VISIBLE else View.GONE
        
        // Show/hide area name for current location
        if (isCurrentLocation && !locationData?.area.isNullOrEmpty()) {
            binding.tvAreaName.text = locationData?.area
            binding.tvAreaName.visibility = View.VISIBLE
            // Adjust layout after setting area name
            binding.tvAreaName.post { adjustHeroLayoutForContent() }
        } else {
            binding.tvAreaName.visibility = View.GONE
        }
    }

    // Missing method implementations
    private fun onCardClick(card: WeatherCard) {
        // Handle card click - for future implementation (navigation to detailed views)
        Toast.makeText(requireContext(), "Clicked on ${card.title}", Toast.LENGTH_SHORT).show()
    }

    private fun requestLocationPermissions() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun openWidgetSelector() {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(requireContext())
            val myProvider = ComponentName(requireContext(), WeatherWidgetProvider::class.java)

            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                appWidgetManager.requestPinAppWidget(myProvider, null, null)
            } else {
                // Fallback for devices that don't support pinning
                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
                startActivity(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Widget selector not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun testWidgetUpdate() {
        try {
            val intent = Intent(requireContext(), WeatherWidgetProvider::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            requireContext().sendBroadcast(intent)
            Toast.makeText(requireContext(), "Widget update triggered", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Widget update failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(uiState: WeatherCardUiState) {
        binding.swipeRefresh.isRefreshing = uiState.isRefreshing
        
        if (uiState.error != null) {
            Snackbar.make(binding.root, uiState.error, Snackbar.LENGTH_LONG).show()
        }
        
        // Update weather cards
        cardsAdapter.submitList(uiState.cards)
    }

    private fun updatePermissionStatus(permissionStatus: MainViewModel.PermissionStatus) {
        when (permissionStatus) {
            is MainViewModel.PermissionStatus.Granted -> {
                // Permission granted - hide permission UI if it exists
                binding.btnRequestPermission?.visibility = View.GONE
            }
            is MainViewModel.PermissionStatus.Denied -> {
                // Permission denied - show request button if it exists
                binding.btnRequestPermission?.visibility = View.VISIBLE
                binding.btnRequestPermission?.text = "Grant Location Permission"
            }
            is MainViewModel.PermissionStatus.Checking -> {
                // Checking permissions - hide UI
                binding.btnRequestPermission?.visibility = View.GONE
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (::videoBackground.isInitialized) {
            try {
                if (videoBackground.isPlaying) {
                    videoBackground.pause()
                }
            } catch (e: IllegalStateException) {
                // Video view not in valid state
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::videoBackground.isInitialized) {
            try {
                if (!videoBackground.isPlaying) {
                    videoBackground.start()
                }
            } catch (e: IllegalStateException) {
                // Video view not in valid state
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::videoBackground.isInitialized) {
            try {
                videoBackground.stopPlayback()
            } catch (e: IllegalStateException) {
                // Video view not in valid state
            }
        }
        _binding = null
    }
} 