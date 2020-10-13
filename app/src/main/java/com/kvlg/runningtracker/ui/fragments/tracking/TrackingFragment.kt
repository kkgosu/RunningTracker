package com.kvlg.runningtracker.ui.fragments.tracking

import android.content.Context
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
import com.kvlg.runningtracker.databinding.FragmentTrackingBinding
import com.kvlg.runningtracker.services.TrackingService
import com.kvlg.runningtracker.ui.fragments.common.ConfirmationDialog
import com.kvlg.runningtracker.ui.main.MainViewModel
import com.kvlg.runningtracker.utils.BnvVisibilityListener
import com.kvlg.runningtracker.utils.Constants
import com.kvlg.runningtracker.utils.TrackingUtils
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment with tracking path via google mapView
 *
 * @author Konstantin Koval
 * @since 26.08.2020
 */
@AndroidEntryPoint
class TrackingFragment : Fragment() {
    private var _binding: FragmentTrackingBinding? = null
    private val binding
        get() = _binding!!

    private val mainViewModel: MainViewModel by viewModels()

    private var map: GoogleMap? = null
    private var isTracking = false
    private var isFromResume = false

    private val constraintSet = ConstraintSet()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as BnvVisibilityListener).hide(true)
    }

    override fun onDetach() {
        (requireActivity() as BnvVisibilityListener).hide(false)
        super.onDetach()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync {
            map = it
        }
        if (savedInstanceState != null) {
            val cancelTrackingDialog = parentFragmentManager.findFragmentByTag(ConfirmationDialog.TAG) as? ConfirmationDialog
            cancelTrackingDialog?.setListener { stopRun() }
        }
        setupToolbar()
        setupStartStopButton()
        setupMoreDataButton()
        subscribeToObservers()
        sendCommandToService(Constants.ACTION_CLEAR_VALUE_SERVICE)
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
        isFromResume = true
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
        binding.toolbar.setNavigationOnClickListener {
            if (isTracking)
                showCancelTrackingDialog()
            else
                requireActivity().onBackPressed()
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
                    TrackingService.zoomWholeMap()
                    TrackingService.endRun()
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
        TrackingService.isTracking.observe(viewLifecycleOwner) {
            updateTracking(it)
        }
        TrackingService.timerFormattedText.observe(viewLifecycleOwner) {
            binding.timerTextView.text = it
        }
        TrackingService.distanceText.observe(viewLifecycleOwner) {
            binding.distanceValueTextView.text = it
        }
        TrackingService.paceText.observe(viewLifecycleOwner) {
            binding.paceValueTextView.text = it
        }
        TrackingService.cameraUpdateToUserLocation.observe(viewLifecycleOwner) {
            map?.animateCamera(it)
        }
        TrackingService.polylineOptions.observe(viewLifecycleOwner) {
            map?.addPolyline(it)
            if (isFromResume) {
                isFromResume = false
                TrackingService.getAllPolylines().forEach { allPolys ->
                    map?.addPolyline(allPolys)
                }
            }
        }
        TrackingService.run.observe(viewLifecycleOwner) {
            binding.includedStatistics.caloriesValueTextView.text = it.caloriesBurned.toString()
            binding.includedStatistics.speedValueTextView.text = it.avgSpeedInKMH.toString()
            binding.includedStatistics.durationValueTextView.text = TrackingUtils.getFormattedStopWatchTime(it.timeInMillis)
            binding.includedStatistics.paceValueTextView.text = TrackingUtils.getFormattedPaceTime(it.avgPaceTime)
            map?.snapshot { bitmap ->
                mainViewModel.saveImageOnDisk(bitmap, it)
                stopRun()
            }
        }
        TrackingService.latLngBounds.observe(viewLifecycleOwner) {
            map?.moveCamera(
                CameraUpdateFactory.newLatLngBounds(
                    it,
                    binding.mapView.width,
                    binding.mapView.height,
                    (binding.mapView.height * 0.05f).toInt()
                )
            )
        }
        mainViewModel.savedRun.observe(viewLifecycleOwner) {
            Snackbar.make(
                requireActivity().findViewById(R.id.root_view),
                getString(R.string.saved_run_snackbar),
                Snackbar.LENGTH_LONG
            ).show()
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
        ConfirmationDialog.newInstance(R.string.cancel_run_alert_title, R.string.cancel_run_alert_desc).apply {
            setListener { stopRun() }
        }.show(parentFragmentManager, ConfirmationDialog.TAG)
    }
}