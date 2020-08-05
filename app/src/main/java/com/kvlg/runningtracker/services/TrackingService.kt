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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.observe
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.kvlg.runningtracker.R
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
import javax.inject.Inject

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

    lateinit var currentNotificationBuilder: NotificationCompat.Builder

    private var isFirstRun = true
    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L

    private val timeRunInSeconds = MutableLiveData<Long>()

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
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
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
        currentNotificationBuilder = baseNotificationBuilder.addAction(R.drawable.ic_pause_24, notificationActionText, pendingIntent)
        notificationManager?.notify(NOTIFICATION_ID, currentNotificationBuilder.build())
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
                pathPoints.postValue(this)
            }
        }
    }

    private fun addEmptyPolyline() {
        pathPoints.value?.apply {
            add(mutableListOf())
            pathPoints.postValue(this)
        } ?: pathPoints.postValue(mutableListOf(mutableListOf()))
    }

    private fun startForegroundService() {
        startTimer()
        isTracking.postValue(true)
        val notificationManager = getSystemService<NotificationManager>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager?.let {
                createNotificationChannel(it)
            }
        }

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        timeRunInSeconds.observe(this) {
            val notification = currentNotificationBuilder.setContentText(TrackingUtils.getFormattedStopWatchTime(it * 1000L, false))
            notificationManager?.notify(NOTIFICATION_ID, notification.build())
        }
    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun startTimer() {
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                lapTime = System.currentTimeMillis() - timeStarted
                timeRunInMillis.postValue(timeRun + lapTime)
                if (timeRunInMillis.value!! >= lastSecondTimestamp + SECOND) {
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
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
        val timeRunInMillis = MutableLiveData<Long>()

        private const val SECOND = 1000L
    }
}