package com.weather.clearsky.feature.settings.presentation.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.weather.clearsky.feature.settings.databinding.FragmentSettingsBinding
import com.weather.clearsky.feature.settings.presentation.adapter.SettingsAdapter
import com.weather.clearsky.feature.settings.presentation.model.SettingAction
import com.weather.clearsky.feature.settings.presentation.model.SettingItem
import com.weather.clearsky.feature.settings.presentation.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var settingsAdapter: SettingsAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observeViewModel()
    }
    
    private fun setupRecyclerView() {
        settingsAdapter = SettingsAdapter(
            onSwitchChanged = { setting, isChecked ->
                handleSwitchSetting(setting, isChecked)
            },
            onListItemClicked = { setting, selectedIndex ->
                handleListSetting(setting, selectedIndex)
            },
            onActionClicked = { setting ->
                handleActionSetting(setting)
            }
        )
        
        binding.rvSettings.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = settingsAdapter
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    settingsAdapter.submitList(uiState.settings)
                    
                    // Show loading state
                    binding.progressBar.visibility = 
                        if (uiState.isLoading) View.VISIBLE else View.GONE
                    
                    // Show error/message
                    uiState.error?.let { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    private fun handleSwitchSetting(setting: SettingItem.SwitchSetting, isChecked: Boolean) {
        when (setting.key) {
            "show_temperature_card" -> viewModel.onCardVisibilityChanged("temperature", isChecked)
            "show_air_quality_card" -> viewModel.onCardVisibilityChanged("air_quality", isChecked)
            "show_humidity_card" -> viewModel.onCardVisibilityChanged("humidity", isChecked)
            "show_wind_card" -> viewModel.onCardVisibilityChanged("wind", isChecked)
            "show_uv_index_card" -> viewModel.onCardVisibilityChanged("uv_index", isChecked)
            "show_forecast_card" -> viewModel.onCardVisibilityChanged("forecast", isChecked)
            "enable_notifications" -> viewModel.onNotificationsChanged(isChecked)
            "enable_location_services" -> viewModel.onLocationServicesChanged(isChecked)
            "enable_widget_updates" -> viewModel.onWidgetUpdatesChanged(isChecked)
        }
    }
    
    private fun handleListSetting(setting: SettingItem.ListSetting, selectedIndex: Int) {
        when (setting.key) {
            "temperature_unit" -> viewModel.onTemperatureUnitChanged(selectedIndex)
            "theme_mode" -> viewModel.onThemeModeChanged(selectedIndex)
            "update_frequency" -> viewModel.onUpdateFrequencyChanged(selectedIndex)
        }
    }
    
    private fun handleActionSetting(setting: SettingItem.ActionSetting) {
        when (setting.action) {
            SettingAction.RESET_SETTINGS -> showResetConfirmDialog()
            SettingAction.CLEAR_CACHE -> showClearCacheConfirmDialog()
            SettingAction.EXPORT_SETTINGS -> viewModel.onActionClicked(SettingAction.EXPORT_SETTINGS)
            SettingAction.RATE_APP -> openPlayStore()
            SettingAction.PRIVACY_POLICY -> openPrivacyPolicy()
            else -> viewModel.onActionClicked(setting.action)
        }
    }
    
    private fun showResetConfirmDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(com.weather.clearsky.feature.settings.R.string.settings_reset_dialog_title)
            .setMessage(com.weather.clearsky.feature.settings.R.string.settings_reset_dialog_message)
            .setPositiveButton(com.weather.clearsky.feature.settings.R.string.reset) { _, _ ->
                viewModel.onActionClicked(SettingAction.RESET_SETTINGS)
            }
            .setNegativeButton(com.weather.clearsky.feature.settings.R.string.cancel, null)
            .show()
    }
    
    private fun showClearCacheConfirmDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(com.weather.clearsky.feature.settings.R.string.settings_clear_cache_dialog_title)
            .setMessage(com.weather.clearsky.feature.settings.R.string.settings_clear_cache_dialog_message)
            .setPositiveButton(com.weather.clearsky.feature.settings.R.string.clear) { _, _ ->
                viewModel.onActionClicked(SettingAction.CLEAR_CACHE)
            }
            .setNegativeButton(com.weather.clearsky.feature.settings.R.string.cancel, null)
            .show()
    }
    
    private fun openPlayStore() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${requireContext().packageName}"))
            startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${requireContext().packageName}"))
            startActivity(intent)
        }
    }
    
    private fun openPrivacyPolicy() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://your-privacy-policy-url.com"))
        startActivity(intent)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 