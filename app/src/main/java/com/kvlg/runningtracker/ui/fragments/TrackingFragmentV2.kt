package com.kvlg.runningtracker.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap
import com.kvlg.runningtracker.R
import com.kvlg.runningtracker.databinding.FragmentTrackingV2Binding
import dagger.hilt.android.AndroidEntryPoint

/**
 * @author Konstantin Koval
 * @since 26.08.2020
 */
@AndroidEntryPoint
class TrackingFragmentV2 : Fragment() {
    private var _binding: FragmentTrackingV2Binding? = null
    private val binding = _binding!!

    private var map: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrackingV2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.navigationIcon?.mutate()?.let {
            it.setTint(ContextCompat.getColor(requireContext(), R.color.gray_800))
            binding.toolbar.navigationIcon = it
        }
        binding.mapView.getMapAsync {
            map = it
        }
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
        _binding = null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        binding.mapView.onSaveInstanceState(outState)
    }

}