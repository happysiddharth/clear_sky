package com.weather.clearsky.feature.alerts.presentation.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs

import com.google.android.material.snackbar.Snackbar
import com.weather.clearsky.feature.alerts.databinding.FragmentCreateAlertBinding
import com.weather.clearsky.feature.alerts.domain.entity.*
import com.weather.clearsky.feature.alerts.presentation.viewmodel.CreateAlertViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.Calendar

@AndroidEntryPoint
class CreateAlertFragment : Fragment() {
    
    private var _binding: FragmentCreateAlertBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: CreateAlertViewModel by viewModels()
    private val args: CreateAlertFragmentArgs by navArgs()
    
    // Spinner adapters
    private lateinit var alertTypeAdapter: ArrayAdapter<String>
    private lateinit var operatorAdapter: ArrayAdapter<String>
    private lateinit var weatherConditionAdapter: ArrayAdapter<String>
    private lateinit var repeatIntervalAdapter: ArrayAdapter<String>
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateAlertBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupSpinners()
        setupTextWatchers()
        setupClickListeners()
        setupCheckboxListeners()
        observeViewModel()
        
        // Handle edit mode if alertId is provided
        args.alertId?.let { alertId ->
            viewModel.loadAlertForEditing(alertId)
        }
    }
    
    private fun setupSpinners() {
        // Alert Type Spinner
        val alertTypes = AlertType.values().map { "${it.icon} ${it.displayName}" }
        alertTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, alertTypes)
        alertTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerAlertType.adapter = alertTypeAdapter
        
        binding.spinnerAlertType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedType = AlertType.values()[position]
                viewModel.updateAlertType(selectedType)
                updateConditionVisibility(selectedType)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Operator Spinner
        val operators = ComparisonOperator.values().map { it.displayName }
        operatorAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, operators)
        operatorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerConditionOperator.adapter = operatorAdapter
        
        binding.spinnerConditionOperator.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedOperator = ComparisonOperator.values()[position]
                viewModel.updateOperator(selectedOperator)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Weather Condition Spinner
        val weatherTypes = WeatherType.values().map { "${it.displayName} - ${it.description}" }
        weatherConditionAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, weatherTypes)
        weatherConditionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerWeatherCondition.adapter = weatherConditionAdapter
        
        binding.spinnerWeatherCondition.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedWeatherType = WeatherType.values()[position]
                viewModel.updateWeatherType(selectedWeatherType)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Repeat Interval Spinner
        val repeatIntervals = RepeatInterval.values().map { it.displayName }
        repeatIntervalAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, repeatIntervals)
        repeatIntervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRepeatInterval.adapter = repeatIntervalAdapter
        
        binding.spinnerRepeatInterval.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedInterval = RepeatInterval.values()[position]
                viewModel.updateRepeatInterval(selectedInterval)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun setupTextWatchers() {
        binding.etAlertTitle.addTextChangedListener { text ->
            viewModel.updateTitle(text.toString())
        }
        
        binding.etAlertDescription.addTextChangedListener { text ->
            viewModel.updateDescription(text.toString())
        }
        
        binding.etConditionValue.addTextChangedListener { text ->
            viewModel.updateConditionValue(text.toString())
        }
        
        binding.etLocation.addTextChangedListener { text ->
            viewModel.updateLocation(text.toString())
        }
    }
    
    private fun setupClickListeners() {
        // Current location button
        binding.btnCurrentLocation.setOnClickListener {
            viewModel.useCurrentLocation()
        }
        
        // Date and time pickers
        binding.etTargetDate.setOnClickListener {
            showDatePicker { year, month, day ->
                viewModel.updateTargetDate(year, month, day)
            }
        }
        
        binding.etTargetTime.setOnClickListener {
            showTimePicker { hour, minute ->
                viewModel.updateTargetTime(hour, minute)
            }
        }
        
        binding.etExpiryDate.setOnClickListener {
            showDatePicker { year, month, day ->
                viewModel.updateExpiryDate(year, month, day)
            }
        }
        
        binding.etExpiryTime.setOnClickListener {
            showTimePicker { hour, minute ->
                viewModel.updateExpiryTime(hour, minute)
            }
        }
        
        // Action buttons
        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.btnCreateAlert.setOnClickListener {
            viewModel.createAlert()
        }
    }
    
    private fun setupCheckboxListeners() {
        binding.cbHasExpiry.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setHasExpiry(isChecked)
            binding.llExpiryContainer.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        
        binding.cbIsRepeating.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setIsRepeating(isChecked)
            binding.spinnerRepeatInterval.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        
        binding.cbNotificationEnabled.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setNotificationEnabled(isChecked)
        }
        
        binding.cbNotificationSound.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setNotificationSound(isChecked)
        }
        
        binding.cbNotificationVibration.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setNotificationVibration(isChecked)
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        updateUI(state)
                    }
                }
                
                launch {
                    viewModel.currentLocation.collect { location ->
                        location?.let {
                            binding.etLocation.setText(it.displayName)
                        }
                    }
                }
            }
        }
    }
    
    private fun updateUI(state: com.weather.clearsky.feature.alerts.presentation.viewmodel.CreateAlertUiState) {
        // Update form fields
        if (binding.etAlertTitle.text.toString() != state.title) {
            binding.etAlertTitle.setText(state.title)
        }
        
        if (binding.etAlertDescription.text.toString() != state.description) {
            binding.etAlertDescription.setText(state.description)
        }
        
        if (binding.etConditionValue.text.toString() != state.conditionValue) {
            binding.etConditionValue.setText(state.conditionValue)
        }
        
        if (binding.etLocation.text.toString() != state.locationString) {
            binding.etLocation.setText(state.locationString)
        }
        
        // Update date/time fields
        binding.etTargetDate.setText(state.targetDateString)
        binding.etTargetTime.setText(state.targetTimeString)
        binding.etExpiryDate.setText(state.expiryDateString)
        binding.etExpiryTime.setText(state.expiryTimeString)
        
        // Update spinners
        binding.spinnerAlertType.setSelection(AlertType.values().indexOf(state.selectedAlertType))
        binding.spinnerConditionOperator.setSelection(ComparisonOperator.values().indexOf(state.selectedOperator))
        
        state.selectedWeatherType?.let { weatherType ->
            binding.spinnerWeatherCondition.setSelection(WeatherType.values().indexOf(weatherType))
        }
        
        binding.spinnerRepeatInterval.setSelection(RepeatInterval.values().indexOf(state.selectedRepeatInterval))
        
        // Update checkboxes
        binding.cbHasExpiry.isChecked = state.hasExpiry
        binding.cbIsRepeating.isChecked = state.isRepeating
        binding.cbNotificationEnabled.isChecked = state.isNotificationEnabled
        binding.cbNotificationSound.isChecked = state.notificationSound
        binding.cbNotificationVibration.isChecked = state.notificationVibration
        
        // Update button state
        binding.btnCreateAlert.isEnabled = state.isFormValid && !state.isLoading
        
        // Show/hide loading and update button text based on edit mode
        if (state.isLoading) {
            binding.btnCreateAlert.text = if (state.isEditMode) "Updating..." else "Creating..."
        } else {
            binding.btnCreateAlert.text = if (state.isEditMode) "Update Alert" else "Create Alert"
        }
        
        // Handle error
        state.error?.let { error ->
            showError(error)
            viewModel.clearError()
        }
        
        // Handle success
        if (state.isAlertCreated) {
            val message = if (state.isEditMode) "Alert updated successfully!" else "Alert created successfully!"
            showSuccess(message)
            viewModel.resetCreatedState()
            findNavController().navigateUp()
        }
        
        // Update condition visibility
        updateConditionVisibility(state.selectedAlertType)
        
        // Update header title based on edit mode
        binding.tvHeaderTitle.text = if (state.isEditMode) "Edit Weather Alert" else "Create Weather Alert"
    }
    
    private fun updateConditionVisibility(alertType: AlertType) {
        when (alertType) {
            AlertType.WEATHER_CONDITION -> {
                binding.spinnerConditionOperator.visibility = View.GONE
                binding.tilConditionValue.visibility = View.GONE
                binding.spinnerWeatherCondition.visibility = View.VISIBLE
            }
            else -> {
                binding.spinnerConditionOperator.visibility = View.VISIBLE
                binding.tilConditionValue.visibility = View.VISIBLE
                binding.spinnerWeatherCondition.visibility = View.GONE
                
                // Update hint based on alert type
                val hint = when (alertType) {
                    AlertType.TEMPERATURE -> "Temperature (°C)"
                    AlertType.RAIN -> "Rain (mm/h)"
                    AlertType.SNOW -> "Snow (mm/h)"
                    AlertType.WIND -> "Wind Speed (km/h)"
                    AlertType.HUMIDITY -> "Humidity (%)"
                    AlertType.UV_INDEX -> "UV Index (0-11)"
                    AlertType.PRESSURE -> "Pressure (hPa)"
                    AlertType.VISIBILITY -> "Visibility (km)"
                    else -> "Value"
                }
                binding.tilConditionValue.hint = hint
            }
        }
    }
    
    private fun showDatePicker(onDateSelected: (year: Int, month: Int, day: Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                onDateSelected(year, month + 1, dayOfMonth) // Month is 0-based in DatePicker
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        // Set minimum date to today
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
    
    private fun showTimePicker(onTimeSelected: (hour: Int, minute: Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                onTimeSelected(hourOfDay, minute)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true // 24-hour format
        )
        timePickerDialog.show()
    }
    
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(resources.getColor(com.weather.clearsky.feature.alerts.R.color.primary_color, null))
            .show()
    }
    
    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(resources.getColor(com.weather.clearsky.feature.alerts.R.color.accent_color, null))
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 