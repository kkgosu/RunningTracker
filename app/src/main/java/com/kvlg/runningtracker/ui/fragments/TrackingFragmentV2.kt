package com.kvlg.runningtracker.ui.fragments

import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.kvlg.runningtracker.R
import com.kvlg.runningtracker.databinding.FragmentTrackingV2Binding
import com.kvlg.runningtracker.db.Run
import com.kvlg.runningtracker.services.Polyline
import com.kvlg.runningtracker.services.TrackingService
import com.kvlg.runningtracker.ui.viewmodels.MainViewModel
import com.kvlg.runningtracker.utils.BnvVisibilityListener
import com.kvlg.runningtracker.utils.Constants
import com.kvlg.runningtracker.utils.TrackingUtils
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlin.math.round

/**
 * @author Konstantin Koval
 * @since 26.08.2020
 */
@AndroidEntryPoint
class TrackingFragmentV2 : Fragment() {
    private var _binding: FragmentTrackingV2Binding? = null
    private val binding
        get() = _binding!!

    private val viewModel: MainViewModel by viewModels()

    private var map: GoogleMap? = null
    private var currentTimeInMillis = 0L
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()

    @set:Inject
    var weight = 80f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrackingV2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity() as BnvVisibilityListener).hide(true)
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync {
            map = it
            addAllPolylines()
        }
        if (savedInstanceState != null) {
            val cancelTrackingDialog = parentFragmentManager.findFragmentByTag(DIALOG_TAG) as? CancelTrackingDialog
            cancelTrackingDialog?.setListener { stopRun() }
        }
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
        binding.startStopButton.setOnClickListener {
            toggleRun()
        }
        binding.startStopButton.setOnLongClickListener {
            if (isTracking) {
                binding.startStopButton.visibility = View.GONE
                binding.startStopLabelTextView.visibility = View.GONE
                binding.moreDataButton.visibility = View.VISIBLE
                binding.moreDataLabelTextView.visibility = View.VISIBLE
                zoomToSeeWholeTrack()
                endRunAndSaveToDb()
            }
            true
        }
        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        viewModel.onBackPressed.observe(viewLifecycleOwner) {
            if (isTracking) showCancelTrackingDialog()
        }

        TrackingService.isTracking.observe(viewLifecycleOwner) {
            updateTracking(it)
        }

        TrackingService.pathPoints.observe(viewLifecycleOwner) {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUserLocation()
        }

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner) {
            currentTimeInMillis = it
            val formattedTime = TrackingUtils.getFormattedStopWatchTime(currentTimeInMillis, true)
            binding.timerTextView.text = formattedTime
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
        if (!isTracking) {
            binding.startStopButton.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.avd_pause_to_play))
            (binding.startStopButton.drawable as AnimatedVectorDrawable).start()
        } else {
            binding.startStopButton.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.avd_play_to_pause))
            (binding.startStopButton.drawable as AnimatedVectorDrawable).start()
        }
    }

    private fun endRunAndSaveToDb() {
        map?.snapshot { bitmap ->
            var distanceInMeters = 0
            pathPoints.forEach {
                distanceInMeters += TrackingUtils.calculatePolylineLength(it).toInt()
            }
            val avgSpeed = round((distanceInMeters / 1000f) / (currentTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((MET * weight * 3.5f) / 200) * (currentTimeInMillis / 1000f / 60)
            val run = Run(bitmap, dateTimeStamp, avgSpeed, distanceInMeters, currentTimeInMillis, caloriesBurned.toInt())
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.root_view),
                getString(R.string.saved_run_snackbar),
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }

    private fun moveCameraToUserLocation() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            if (pathPoints.last().size == 1 && pathPoints.size == 1) {
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(pathPoints.last().last(), Constants.MAP_ZOOM))
            } else {
                map?.animateCamera(CameraUpdateFactory.newLatLng(pathPoints.last().last()))
            }
        }
    }

    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        pathPoints.forEach { polyline ->
            polyline.forEach {
                bounds.include(it)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = getPolylineOptions()
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun addAllPolylines() {
        pathPoints.forEach {
            val polylineOptions = getPolylineOptions().addAll(it)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun getPolylineOptions(): PolylineOptions = PolylineOptions()
        .color(Constants.POLYLINE_COLOR)
        .width(Constants.POLYLINE_WIDTH)

    private fun showCancelTrackingDialog() {
        CancelTrackingDialog().apply {
            setListener { stopRun() }
        }.show(parentFragmentManager, DIALOG_TAG)
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

    override fun onDestroyView() {
        (requireActivity() as BnvVisibilityListener).hide(false)
        super.onDestroyView()
    }

    companion object {
        private const val DIALOG_TAG = "CancelDialogTag"
        private const val MET = 9
    }

}