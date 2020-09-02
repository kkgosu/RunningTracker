package com.kvlg.runningtracker.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.kvlg.runningtracker.R
import com.kvlg.runningtracker.databinding.FragmentTrackingBinding
import com.kvlg.runningtracker.db.Run
import com.kvlg.runningtracker.services.Polyline
import com.kvlg.runningtracker.services.TrackingService
import com.kvlg.runningtracker.ui.viewmodels.MainViewModel
import com.kvlg.runningtracker.utils.Constants
import com.kvlg.runningtracker.utils.TrackingUtils
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlin.math.round

/**
 * Fragment with map and tracking service
 *
 * @author Konstantin Koval
 * @since 19.07.2020
 */
@AndroidEntryPoint
class TrackingFragment : Fragment() {
    private val viewModel: MainViewModel by viewModels()

    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!
    private var menu: Menu? = null

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private var map: GoogleMap? = null
    private var currentTimeInMillis = 0L

    @set:Inject
    var weight = 80f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        _binding = FragmentTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync {
            map = it
            addAllPolylines()
        }
        if (savedInstanceState != null) {
            val cancelTrackingDialog = parentFragmentManager.findFragmentByTag(DIALOG_TAG) as? CancelTrackingDialog
            cancelTrackingDialog?.setListener { stopRun() }
        }

        binding.startRunButton.setOnClickListener {
            toggleRun()
        }
        binding.finishRunButton.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
        }
        subscribeToObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if (currentTimeInMillis > 0) {
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.mi_tracking_cancel -> {
            showCancelTrackingDialog()
            true
        }
        else -> super.onOptionsItemSelected(item)
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

    private fun subscribeToObservers() {
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
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(Constants.ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(Constants.ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun stopRun() {
        binding.timerTextView.text = getString(R.string._00_00_00_00)
        sendCommandToService(Constants.ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking && currentTimeInMillis > 0L) {
            binding.startRunButton.text = getString(R.string.resume_run)
            binding.finishRunButton.visibility = View.VISIBLE
        } else if (!isTracking) {
            binding.startRunButton.text = getString(R.string.start_run)
            binding.finishRunButton.visibility = View.GONE
        } else if (isTracking) {
            menu?.getItem(0)?.isVisible = true
            binding.startRunButton.text = getString(R.string.pause_run)
            binding.finishRunButton.visibility = View.GONE
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

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    private fun getPolylineOptions(): PolylineOptions = PolylineOptions()
        .color(Constants.POLYLINE_COLOR)
        .width(Constants.POLYLINE_WIDTH)

    private fun showCancelTrackingDialog() {
        CancelTrackingDialog().apply {
            setListener { stopRun() }
        }.show(parentFragmentManager, DIALOG_TAG)
    }

    companion object {
        private const val DIALOG_TAG = "CancelDialogTag"
        private const val MET = 9
    }

}