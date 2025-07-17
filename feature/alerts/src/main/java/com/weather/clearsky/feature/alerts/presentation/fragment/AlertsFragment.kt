package com.weather.clearsky.feature.alerts.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.weather.clearsky.feature.alerts.databinding.FragmentAlertsBinding
import com.weather.clearsky.feature.alerts.domain.entity.AlertStatus
import com.weather.clearsky.feature.alerts.presentation.adapter.AlertsAdapter
import com.weather.clearsky.feature.alerts.presentation.viewmodel.AlertsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.util.Log
import com.weather.clearsky.feature.alerts.R

@AndroidEntryPoint
class AlertsFragment : Fragment() {
    
    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AlertsViewModel by viewModels()
    private lateinit var alertsAdapter: AlertsAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupFilterChips()
        setupClickListeners()
        observeViewModel()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh alerts when fragment becomes visible again
        Log.d("AlertsFragment", "Fragment resumed, refreshing alerts")
        viewModel.refreshAlerts()
    }
    
    private fun setupRecyclerView() {
        alertsAdapter = AlertsAdapter(
            onEditClick = { alert ->
                // Navigate to edit alert fragment with alertId argument
                val bundle = bundleOf("alertId" to alert.id)
                findNavController().navigate(com.weather.clearsky.feature.alerts.R.id.action_alerts_to_edit_alert, bundle)
            },
            onDeleteClick = { alert ->
                showDeleteConfirmationDialog(alert.id)
            },
            onStatusToggle = { alert ->
                viewModel.toggleAlertStatus(alert.id, alert.status)
            }
        )
        
        binding.recyclerAlerts.apply {
            adapter = alertsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }
    
    private fun setupFilterChips() {
        with(binding) {
            chipActive.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    viewModel.setStatusFilter(AlertStatus.ACTIVE)
                } else {
                    viewModel.clearFilters()
                }
            }
            
            chipTriggered.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    viewModel.setStatusFilter(AlertStatus.TRIGGERED)
                } else {
                    viewModel.clearFilters()
                }
            }
            
            chipInactive.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    viewModel.setStatusFilter(AlertStatus.INACTIVE)
                } else {
                    viewModel.clearFilters()
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        with(binding) {
            fabAddAlert.setOnClickListener {
                // Navigate to create alert fragment
                findNavController().navigate(R.id.action_alerts_to_create_alert)
            }
            
            btnClearFilters.setOnClickListener {
                viewModel.clearFilters()
                chipGroupFilters.clearCheck()
            }
            
            // Debug: Long press to clear all alerts (temporary for fixing corrupted data)
            btnClearFilters.setOnLongClickListener {
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Clear All Alerts")
                    .setMessage("This will delete ALL alerts permanently. Are you sure?")
                    .setPositiveButton("Clear All") { _, _ ->
                        viewModel.clearAllAlerts()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
                true
            }
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }
    
    private fun updateUI(state: com.weather.clearsky.feature.alerts.presentation.viewmodel.AlertsUiState) {
        with(binding) {
            // Update loading state
            progressLoading.visibility = if (state.isLoading) View.VISIBLE else View.GONE
            
            // Update alerts list
            Log.d("AlertsFragment", "Updating UI with ${state.alerts.size} alerts")
            alertsAdapter.submitList(state.alerts)
            
            // Show/hide empty state
            if (state.alerts.isEmpty() && !state.isLoading) {
                Log.d("AlertsFragment", "Showing empty state")
                emptyState.visibility = View.VISIBLE
                recyclerAlerts.visibility = View.GONE
            } else {
                Log.d("AlertsFragment", "Showing alerts list")
                emptyState.visibility = View.GONE
                recyclerAlerts.visibility = View.VISIBLE
            }
            
            // Show error if any
            state.error?.let { error ->
                showError(error)
                viewModel.clearError()
            }
        }
    }
    
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Retry") {
                viewModel.refreshAlerts()
            }
            .show()
    }
    
    private fun showDeleteConfirmationDialog(alertId: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Alert")
            .setMessage("Are you sure you want to delete this alert? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteAlert(alertId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 