package com.kvlg.runningtracker.utils

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Build
import com.kvlg.runningtracker.services.Polyline
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit

/**
 * Utils for tracking
 *
 * @author Konstantin Koval
 * @since 24.07.2020
 */
object TrackingUtils {

    fun hasLocationPermissions(context: Context): Boolean = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        EasyPermissions.hasPermissions(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        EasyPermissions.hasPermissions(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    }

    fun calculatePolylineLength(polyline: Polyline): Float {
        val result = FloatArray(1)
        return polyline.mapIndexed { index, latLng ->
            if (index < polyline.size - 1) {
                Location.distanceBetween(
                    latLng.latitude,
                    latLng.longitude,
                    polyline[index + 1].latitude,
                    polyline[index + 1].longitude,
                    result
                )
                println("result = ${result[0]}")
                result[0]
            } else {
                0f
            }
        }.sum()
    }

    fun getFormattedStopWatchTime(ms: Long, includeMillis: Boolean = false): String {
        var millis = ms
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        millis -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        millis -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis)
        if (!includeMillis) {
            return getFormattedTime(hours, minutes, seconds)
        }
        millis -= TimeUnit.SECONDS.toMillis(seconds)
        millis /= 10
        return "${getFormattedTime(hours, minutes, seconds)}:${millis.formatTime()}"
    }

    private fun getFormattedTime(hours: Long, minutes: Long, seconds: Long): String {
        return "${hours.formatTime()}:" +
                "${minutes.formatTime()}:" +
                seconds.formatTime()
    }

    private fun Long.formatTime(): String = if (this < 10) "0$this" else this.toString()
}