package com.kvlg.runningtracker.ui.fragments

import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.material.snackbar.Snackbar
import com.kvlg.runningtracker.R
import com.kvlg.runningtracker.databinding.FragmentTrackingV2Binding
import com.kvlg.runningtracker.services.TrackingService
import com.kvlg.runningtracker.ui.viewmodels.MainViewModel
import com.kvlg.runningtracker.utils.BnvVisibilityListener
import com.kvlg.runningtracker.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

/**
 * @author Konstantin Koval
 * @since 26.08.2020
 */
@AndroidEntryPoint
class TrackingFragmentV2 : Fragment() {
    private var _binding: FragmentTrackingV2Binding? = null
    private val binding
        get() = _binding!!

    private val mainViewModel: MainViewModel by viewModels()
    private val trackingViewModel: TrackingViewModel by viewModels()

    private var map: GoogleMap? = null
    private var isTracking = false

    private val constraintSet = ConstraintSet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity() as BnvVisibilityListener).hide(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrackingV2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync {
            map = it
            trackingViewModel.addAllPolylines()
        }
        if (savedInstanceState != null) {
            val cancelTrackingDialog = parentFragmentManager.findFragmentByTag(DIALOG_TAG) as? CancelTrackingDialog
            cancelTrackingDialog?.setListener { stopRun() }
        }
        setupToolbar()
        setupStartStopButton()
        setupMoreDataButton()
        subscribeToObservers()
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
        (requireActivity() as BnvVisibilityListener).hide(false)
        binding.mapView.onDestroy()
        _binding = null
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        binding.mapView.onSaveInstanceState(outState)
    }

    private fun setupToolbar() {
        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.run {
                setDisplayHomeAsUpEnabled(true)
                title = ""
            }
        }
        binding.toolbar.navigationIcon?.mutate()?.let {
            it.setTint(ContextCompat.getColor(requireContext(), R.color.gray_800))
            binding.toolbar.navigationIcon = it
        }
    }

    private fun setupStartStopButton() {
        with(binding) {
            startStopButton.setOnClickListener {
                toggleRun()
            }
            startStopButton.setOnLongClickListener {
                if (isTracking) {
                    startStopButton.visibility = View.GONE
                    startStopLabelTextView.visibility = View.GONE
                    moreDataButton.visibility = View.VISIBLE
                    moreDataLabelTextView.visibility = View.VISIBLE
                    trackingViewModel.zoomWholeMap()
                    trackingViewModel.endRun()
                }
                true
            }
        }
    }

    private fun setupMoreDataButton() {
        with(constraintSet) {
            clone(binding.trackingRootLayout)
            binding.moreDataButton.setOnClickListener {
                clear(R.id.included_statistics, ConstraintSet.TOP)
                connect(R.id.included_statistics, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
                applyTo(binding.trackingRootLayout)
            }
        }
        binding.includedStatistics.includedStatisticsRoot.setOnClickListener { }
    }

    private fun subscribeToObservers() {
        mainViewModel.onBackPressed.observe(viewLifecycleOwner) {
            if (isTracking) showCancelTrackingDialog()
        }

        TrackingService.isTracking.observe(viewLifecycleOwner) {
            updateTracking(it)
        }

        TrackingService.pathPoints.observe(viewLifecycleOwner) {
            trackingViewModel.populatePathPoints(it)
        }

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner) {
            trackingViewModel.setCurrentTimeInMillis(it)
            trackingViewModel.updateTimerText()
        }

        trackingViewModel.pathPoints.observe(viewLifecycleOwner) {
            trackingViewModel.addLatestPolyline()
            trackingViewModel.moveCameraToUserLocation()
        }
        trackingViewModel.timerFormattedText.observe(viewLifecycleOwner) {
            binding.timerTextView.text = it
        }
        trackingViewModel.cameraUpdateToUserLocation.observe(viewLifecycleOwner) {
            map?.animateCamera(it)
        }
        trackingViewModel.polylineOptions.observe(viewLifecycleOwner) {
            map?.addPolyline(it)
        }
        trackingViewModel.run.observe(viewLifecycleOwner) {
            map?.snapshot { bitmap ->
                val run = it.copy(img = bitmap)
                mainViewModel.insertRun(run)
                Snackbar.make(
                    requireActivity().findViewById(R.id.root_view),
                    getString(R.string.saved_run_snackbar),
                    Snackbar.LENGTH_LONG
                ).show()
                stopRun()
            }
        }
        trackingViewModel.latLngBounds.observe(viewLifecycleOwner) {
            map?.moveCamera(
                CameraUpdateFactory.newLatLngBounds(
                    it,
                    binding.mapView.width,
                    binding.mapView.height,
                    (binding.mapView.height * 0.05f).toInt()
                )
            )
        }
    }

    private fun toggleRun() {
        if (isTracking) {
            sendCommandToService(Constants.ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(Constants.ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun stopRun() {
        sendCommandToService(Constants.ACTION_STOP_SERVICE)
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        with(binding) {
            if (!isTracking) {
                startStopButton.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.avd_pause_to_play))
                (startStopButton.drawable as AnimatedVectorDrawable).start()
                startStopLabelTextView.visibility = View.GONE
            } else {
                startStopButton.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.avd_play_to_pause))
                (startStopButton.drawable as AnimatedVectorDrawable).start()
                startStopLabelTextView.visibility = View.VISIBLE
            }
        }
    }

    private fun showCancelTrackingDialog() {
        CancelTrackingDialog().apply {
            setListener { stopRun() }
        }.show(parentFragmentManager, DIALOG_TAG)
    }

    companion object {
        private const val DIALOG_TAG = "CancelDialogTag"
    }
}