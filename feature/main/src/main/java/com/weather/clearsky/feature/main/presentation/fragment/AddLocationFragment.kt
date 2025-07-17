package com.weather.clearsky.feature.main.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.weather.clearsky.feature.main.databinding.FragmentAddLocationBinding
import com.weather.clearsky.feature.main.data.model.SavedLocation
import com.weather.clearsky.feature.main.presentation.adapter.LocationSearchAdapter
import com.weather.clearsky.feature.main.presentation.viewmodel.AddLocationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.weather.clearsky.core.common.navigation.NavigationCallback

@AndroidEntryPoint
class AddLocationFragment : Fragment() {

    private var _binding: FragmentAddLocationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddLocationViewModel by viewModels()
    private lateinit var searchAdapter: LocationSearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupClickListeners()
        setupSearchField()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        searchAdapter = LocationSearchAdapter { location ->
            onLocationSelected(location)
        }
        
        binding.rvSearchResults.apply {
            adapter = searchAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupClickListeners() {
        binding.btnClose.setOnClickListener {
            // Navigate back to first location in ViewPager2
            (activity as? NavigationCallback)?.navigateToMain()
        }

        binding.btnUseCurrentLocation.setOnClickListener {
            viewModel.addCurrentLocation()
        }
    }

    private fun setupSearchField() {
        binding.etSearch.addTextChangedListener { text ->
            val query = text.toString().trim()
            if (query.length >= 3) {
                viewModel.searchLocations(query)
            } else {
                viewModel.clearSearch()
            }
        }

        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            val query = binding.etSearch.text.toString().trim()
            if (query.length >= 3) {
                viewModel.searchLocations(query)
            }
            true
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.searchResults.collect { results ->
                updateSearchResults(results)
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                updateLoadingState(isLoading)
            }
        }

        lifecycleScope.launch {
            viewModel.addLocationResult.collect { result ->
                result?.let { (success, message, locationId) ->
                    if (success && locationId != null) {
                        Toast.makeText(requireContext(), "Location added successfully!", Toast.LENGTH_SHORT).show()
                        // Navigate to the newly added location
                        (activity as? NavigationCallback)?.navigateToLocation(locationId)
                    } else {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                    }
                    viewModel.clearAddLocationResult()
                }
            }
        }
    }

    private fun updateSearchResults(results: List<SavedLocation>) {
        if (results.isEmpty()) {
            binding.rvSearchResults.visibility = View.GONE
            binding.tvSearchHint.visibility = View.VISIBLE
        } else {
            binding.rvSearchResults.visibility = View.VISIBLE
            binding.tvSearchHint.visibility = View.GONE
            searchAdapter.submitList(results)
        }
    }

    private fun updateLoadingState(isLoading: Boolean) {
        binding.progressLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun onLocationSelected(location: SavedLocation) {
        viewModel.addLocation(location)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 