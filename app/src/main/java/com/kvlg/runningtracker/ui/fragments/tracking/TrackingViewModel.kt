package com.kvlg.runningtracker.ui.fragments.tracking

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.kvlg.runningtracker.db.run.Run
import com.kvlg.runningtracker.services.Polyline
import com.kvlg.runningtracker.services.Polylines
import com.kvlg.runningtracker.utils.Constants
import com.kvlg.runningtracker.utils.TrackingUtils
import java.util.*
import kotlin.math.round

/**
 * @author Konstantin Koval
 * @since 03.09.2020
 */
class TrackingViewModel @ViewModelInject constructor(
    private val weight: Float,
) : ViewModel() {

    private var currentTimeInMillis = 0L
    private var currentDistanceInMeters = 0F

    //for pace calculation
    private var lastKm = 0
    private var lastCurrentTimeInMillis = 0L

    private val _pathPoints = MutableLiveData(mutableListOf<Polyline>())
    val pathPoints: LiveData<MutableList<Polyline>> = _pathPoints

    private val _cameraUpdate = MutableLiveData<CameraUpdate>()
    val cameraUpdateToUserLocation: LiveData<CameraUpdate> = _cameraUpdate

    private val _run = MutableLiveData<Run>()
    val run: LiveData<Run> = _run

    private val _polylineOptions = MutableLiveData<PolylineOptions>()
    val polylineOptions: LiveData<PolylineOptions> = _polylineOptions

    private val _latLngBounds = MutableLiveData<LatLngBounds>()
    val latLngBounds: LiveData<LatLngBounds> = _latLngBounds

    private val _timerFormattedText = MutableLiveData<String>()
    val timerFormattedText: LiveData<String> = _timerFormattedText

    private val _distance = MutableLiveData<String>()
    val distanceText: LiveData<String> = _distance

    private val _pace = MutableLiveData<String>()
    val paceText: LiveData<String> = _pace

    private fun updateDistanceText() {
        _pathPoints.value?.let {
            if (it.isNotEmpty() && it.last().size > 1) {
                val lastTwo = it[0].takeLast(2)
                currentDistanceInMeters += TrackingUtils.calculateDistanceBetweenCoordinates(lastTwo[0], lastTwo[1])
                _distance.value = String.format("%.2f", currentDistanceInMeters / 1000F)

                if ((currentDistanceInMeters / 1000).toInt() == lastKm + 1) {
                    _pace.value = TrackingUtils.getFormattedPaceTime(currentTimeInMillis - lastCurrentTimeInMillis)
                    lastKm++
                    lastCurrentTimeInMillis = currentTimeInMillis
                }
            }
        }
    }

    fun updateTimerText() {
        _timerFormattedText.value = TrackingUtils.getFormattedStopWatchTime(currentTimeInMillis, true)
    }

    fun zoomWholeMap() {
        val bounds = LatLngBounds.Builder()
        _pathPoints.value?.let {
            it.forEach { polyline ->
                polyline.forEach { latLng ->
                    bounds.include(latLng)
                }
            }
        }
        _latLngBounds.value = bounds.build()
    }

    fun addLatestPolyline() {
        _pathPoints.value?.let {
            if (it.isNotEmpty() && it.last().size > 1) {
                val preLastLatLng = it.last()[it.last().size - 2]
                val lastLatLng = it.last().last()
                val polylineOptions = getPolylineOptions()
                    .add(preLastLatLng)
                    .add(lastLatLng)
                _polylineOptions.value = polylineOptions
            }
        }
    }

    fun addAllPolylines() {
        _pathPoints.value?.let { points ->
            points.forEach {
                _polylineOptions.value = getPolylineOptions().addAll(it)
            }
        }
    }

    private fun getPolylineOptions(): PolylineOptions = PolylineOptions()
        .color(Constants.POLYLINE_COLOR)
        .width(Constants.POLYLINE_WIDTH)

    fun populatePathPoints(polylines: Polylines) {
        _pathPoints.value = polylines
        updateDistanceText()
    }

    fun setCurrentTimeInMillis(millis: Long) {
        currentTimeInMillis = millis
    }

    fun endRun() {
        var distanceInMeters = 0
        _pathPoints.value!!.forEach {
            distanceInMeters += TrackingUtils.calculatePolylineLength(it).toInt()
        }
        val avgSpeed = round((distanceInMeters / 1000f) / (currentTimeInMillis / 1000f / 60 / 60) * 10) / 10f
        val dateTimeStamp = Calendar.getInstance().timeInMillis
        val caloriesBurned = ((MET * weight * 3.5f) / 200) * (currentTimeInMillis / 1000f / 60)
        val run = Run(null, dateTimeStamp, avgSpeed, distanceInMeters, currentTimeInMillis, caloriesBurned.toInt())

        _run.value = run
    }

    fun moveCameraToUserLocation() {
        _pathPoints.value?.let {
            if (it.isNotEmpty() && it.last().isNotEmpty()) {
                if (it.last().size == 1 && it.size == 1) {
                    _cameraUpdate.value = CameraUpdateFactory.newLatLngZoom(it.last().last(), Constants.MAP_ZOOM)
                } else {
                    _cameraUpdate.value = CameraUpdateFactory.newLatLng(it.last().last())
                }
            }
        }
    }

    companion object {
        private const val MET = 9
    }
}