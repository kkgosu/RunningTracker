package com.kvlg.runningtracker.services

import android.content.Intent
import androidx.lifecycle.LifecycleService
import com.kvlg.runningtracker.utils.Constants
import timber.log.Timber

/**
 * @author Konstantin Koval
 * @since 26.07.2020
 */
class TrackingService : LifecycleService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                Constants.ACTION_START_OR_RESUME_SERVICE -> {
                    Timber.d("Started or resumed service")
                }
                Constants.ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                }
                Constants.ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

}