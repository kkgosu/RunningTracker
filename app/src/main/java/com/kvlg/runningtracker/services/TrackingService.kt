package com.kvlg.runningtracker.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.kvlg.runningtracker.R
import com.kvlg.runningtracker.db.run.Run
import com.kvlg.runningtracker.utils.Constants
import com.kvlg.runningtracker.utils.Constants.ACTION_PAUSE_SERVICE
import com.kvlg.runningtracker.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import com.kvlg.runningtracker.utils.Constants.ACTION_STOP_SERVICE
import com.kvlg.runningtracker.utils.Constants.FASTEST_UPDATE_LOCATION_INTERVAL
import com.kvlg.runningtracker.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.kvlg.runningtracker.utils.Constants.NOTIFICATION_CHANNEL_NAME
import com.kvlg.runningtracker.utils.Constants.NOTIFICATION_ID
import com.kvlg.runningtracker.utils.Constants.TIMER_UPDATE_INTERVAL
import com.kvlg.runningtracker.utils.Constants.TRACKING_SERVICE_PAUSE_PENDING_INTENT
import com.kvlg.runningtracker.utils.Constants.TRACKING_SERVICE_START_OR_RESUME_PENDING_INTENT
import com.kvlg.runningtracker.utils.Constants.UPDATE_LOCATION_INTERVAL
import com.kvlg.runningtracker.utils.TrackingUtils
import com.kvlg.runningtracker.utils.TrackingUtils.hasLocationPermissions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.math.round

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

/**
 * Service for tracking locations
 *
 * @author Konstantin Koval
 * @since 26.07.2020
 */
@AndroidEntryPoint
class TrackingService : LifecycleService() {

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    @set:Inject
    var weight = 80f

    private lateinit var currentNotificationBuilder: NotificationCompat.Builder

    private var isServiceStopped = false
    private var isFirstRun = true
    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L

    private val timeRunInSeconds = MutableLiveData(0L)

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            if (isTracking.value!!) {
                result?.locations?.let {
                    it.forEach { location ->
                        addPathPoint(location)
                        Timber.d("NEW LOCATION: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        currentNotificationBuilder = baseNotificationBuilder
        postInitialValues()
        addAllPolylines()
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        isTracking.observe(this) {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resuming service..")
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                    stopService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun stopService() {
        isServiceStopped = true
        isFirstRun = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }

    private fun postInitialValues() {
        _isTracking.postValue(false)
        populatePathPoints(mutableListOf())
        _timeRunInSeconds.postValue(0L)
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking && hasLocationPermissions(this)) {
            val request = LocationRequest().apply {
                interval = UPDATE_LOCATION_INTERVAL
                fastestInterval = FASTEST_UPDATE_LOCATION_INTERVAL
                priority = PRIORITY_HIGH_ACCURACY
            }
            fusedLocationProviderClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if (isTracking) getString(R.string.pause_run) else getString(R.string.resume_run)
        val pendingIntent = if (isTracking) {
            getNotificationPendingIntent(ACTION_PAUSE_SERVICE, TRACKING_SERVICE_PAUSE_PENDING_INTENT)
        } else {
            getNotificationPendingIntent(ACTION_START_OR_RESUME_SERVICE, TRACKING_SERVICE_START_OR_RESUME_PENDING_INTENT)
        }

        val notificationManager = getSystemService<NotificationManager>()
        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotificationBuilder, arrayListOf<NotificationCompat.Action>())
        }
        if (!isServiceStopped) {
            currentNotificationBuilder = baseNotificationBuilder.addAction(R.drawable.ic_pause_24, notificationActionText, pendingIntent)
            notificationManager?.notify(NOTIFICATION_ID, currentNotificationBuilder.build())
        }
    }

    private fun getNotificationPendingIntent(actionCode: String, requestCode: Int): PendingIntent =
        Intent(this, TrackingService::class.java).apply {
            action = actionCode
        }.let {
            PendingIntent.getService(this, requestCode, it, FLAG_UPDATE_CURRENT)
        }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                populatePathPoints(this)
            }
        }
    }

    private fun addEmptyPolyline() {
        pathPoints.value?.apply {
            add(mutableListOf())
            populatePathPoints(this)
        } ?: populatePathPoints(mutableListOf(mutableListOf()))
    }

    private fun startForegroundService() {
        startTimer()
        _isTracking.postValue(true)
        val notificationManager = getSystemService<NotificationManager>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager?.let {
                createNotificationChannel(it)
            }
        }

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        timeRunInSeconds.observe(this) {
            if (!isServiceStopped) {
                val notification = currentNotificationBuilder.setContentText(TrackingUtils.getFormattedStopWatchTime(it * 1000L, false))
                notificationManager?.notify(NOTIFICATION_ID, notification.build())
            }
        }
    }

    private fun pauseService() {
        _isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun startTimer() {
        addEmptyPolyline()
        _isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                lapTime = System.currentTimeMillis() - timeStarted
                setCurrentTimeInMillis(timeRun + lapTime)
                if (timeRunInMillis >= lastSecondTimestamp + SECOND) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += SECOND
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val SECOND = 1000L
        private const val MET = 9
        private val _isTracking = MutableLiveData<Boolean>()
        private val _pathPoints = MutableLiveData<Polylines>()
        private val _cameraUpdate = MutableLiveData<CameraUpdate>()
        private val _run = MutableLiveData<Run>()
        private val _polylineOptions = MutableLiveData<PolylineOptions>()
        private val _latLngBounds = MutableLiveData<LatLngBounds>()
        private val _timerFormattedText = MutableLiveData<String>()
        private val _distance = MutableLiveData<String>()
        private val _pace = MutableLiveData<String>()

        private val _timeRunInSeconds = MutableLiveData<Long>()
        private var timeRunInMillis = 0L

        private var currentDistanceInMeters = 0F

        //for pace calculation
        private var lastKm = 0
        private var lastCurrentTimeInMillis = 0L

        private val paceTimes = mutableListOf<Long>()
        val isTracking: LiveData<Boolean> = _isTracking
        val pathPoints: LiveData<MutableList<Polyline>> = _pathPoints
        val cameraUpdateToUserLocation: LiveData<CameraUpdate> = _cameraUpdate
        val run: LiveData<Run> = _run
        val polylineOptions: LiveData<PolylineOptions> = _polylineOptions
        val latLngBounds: LiveData<LatLngBounds> = _latLngBounds
        val timerFormattedText: LiveData<String> = _timerFormattedText
        val distanceText: LiveData<String> = _distance
        val paceText: LiveData<String> = _pace

        private fun updateDistanceText() {
            _pathPoints.value?.let {
                if (it.isNotEmpty() && it.last().size > 1) {
                    val lastTwo = it[0].takeLast(2)
                    currentDistanceInMeters += TrackingUtils.calculateDistanceBetweenCoordinates(lastTwo[0], lastTwo[1])
                    _distance.value = String.format("%.2f", currentDistanceInMeters / 1000F)

                    if ((currentDistanceInMeters / 1000).toInt() == lastKm + 1) {
                        val ms = this.timeRunInMillis - lastCurrentTimeInMillis
                        _pace.value = TrackingUtils.getFormattedPaceTime(ms)
                        paceTimes.add(ms)
                        lastKm++
                        lastCurrentTimeInMillis = this.timeRunInMillis
                    }
                }
            }
        }

        private fun updateTimerText() {
            _timerFormattedText.value = TrackingUtils.getFormattedStopWatchTime(this.timeRunInMillis, true)
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

        private fun addLatestPolyline() {
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
            addLatestPolyline()
            moveCameraToUserLocation()
        }

        fun setCurrentTimeInMillis(millis: Long) {
            this.timeRunInMillis = millis
            updateTimerText()
        }

        fun endRun() {
            var distanceInMeters = 0
            _pathPoints.value!!.forEach {
                distanceInMeters += TrackingUtils.calculatePolylineLength(it).toInt()
            }
            val avgSpeed = round((distanceInMeters / 1000f) / (this.timeRunInMillis / 1000f / 60 / 60) * 10) / 10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((MET * 70 * 3.5f) / 200) * (this.timeRunInMillis / 1000f / 60)
            val avgPaceTime = paceTimes.average().toLong()
            val run = Run(null, dateTimeStamp, avgSpeed, distanceInMeters, this.timeRunInMillis, caloriesBurned.toInt(), avgPaceTime)

            _run.value = run
        }

        private fun moveCameraToUserLocation() {
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
    }
}