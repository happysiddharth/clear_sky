package com.weather.clearsky.feature.main.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.weather.clearsky.feature.main.databinding.FragmentLocationManagerBinding
import com.weather.clearsky.feature.main.data.model.SavedLocation
import com.weather.clearsky.feature.main.presentation.adapter.LocationManagerAdapter
import com.weather.clearsky.feature.main.presentation.viewmodel.LocationsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.weather.clearsky.core.common.navigation.NavigationCallback

@AndroidEntryPoint
class LocationManagerFragment : Fragment() {

    private var _binding: FragmentLocationManagerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LocationsViewModel by viewModels()
    private lateinit var locationAdapter: LocationManagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        locationAdapter = LocationManagerAdapter(
            onMoveUp = { location ->
                viewModel.moveLocationUp(location.id)
            },
            onMoveDown = { location ->
                viewModel.moveLocationDown(location.id)
            },
            onDelete = { location ->
                showDeleteConfirmation(location)
            },
            onLocationClick = { location ->
                // Navigate to this location's weather screen
                (activity as? NavigationCallback)?.navigateToLocation(location.id)
            }
        )
        
        binding.rvLocations.apply {
            adapter = locationAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupClickListeners() {
        binding.btnClose.setOnClickListener {
            // Navigate back to main
            (activity as? NavigationCallback)?.navigateToMain()
        }

        binding.btnAddLocation.setOnClickListener {
            // Navigate to add location fragment
            val addLocationFragment = AddLocationFragment()
            
            parentFragmentManager.beginTransaction()
                .replace(android.R.id.content, addLocationFragment)
                .addToBackStack("add_location")
                .commit()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.savedLocations.collect { locations ->
                locationAdapter.submitList(locations)
            }
        }
    }

    private fun showDeleteConfirmation(location: SavedLocation) {
        // Simple confirmation for now - in a real app, use AlertDialog
        if (location.isCurrentLocation) {
            Toast.makeText(
                requireContext(), 
                "Cannot delete current location", 
                Toast.LENGTH_SHORT
            ).show()
        } else {
            // For now, just delete directly
            viewModel.removeLocation(location.id)
            Toast.makeText(
                requireContext(), 
                "Location '${location.name}' removed", 
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 