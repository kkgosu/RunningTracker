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
import com.kvlg.runningtracker.ui.MainActivity
import com.kvlg.runningtracker.utils.Constants
import com.kvlg.runningtracker.utils.Constants.ACTION_PAUSE_SERVICE
import com.kvlg.runningtracker.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import com.kvlg.runningtracker.utils.Constants.ACTION_STOP_SERVICE
import com.kvlg.runningtracker.utils.Constants.FASTEST_UPDATE_LOCATION_INTERVAL
import com.kvlg.runningtracker.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.kvlg.runningtracker.utils.Constants.NOTIFICATION_CHANNEL_NAME
import com.kvlg.runningtracker.utils.Constants.NOTIFICATION_ID
import com.kvlg.runningtracker.utils.Constants.UPDATE_LOCATION_INTERVAL
import com.kvlg.runningtracker.utils.TrackingUtils.hasLocationPermissions
import timber.log.Timber

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

/**
 * Service for tracking locations
 *
 * @author Konstantin Koval
 * @since 26.07.2020
 */
class TrackingService : LifecycleService() {

    private var isFirstRun = true

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

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
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        isTracking.observe(this) {
            updateLocationTracking(it)
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
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
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
        addEmptyPolyline()
        isTracking.postValue(true)
        val notificationManager = getSystemService<NotificationManager>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager?.let {
                createNotificationChannel(notificationManager)
            }
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_run_24)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string._00_00_00))
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
        }, FLAG_UPDATE_CURRENT
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private val isTracking = MutableLiveData<Boolean>()
        private val pathPoints = MutableLiveData<Polylines>()
    }
}