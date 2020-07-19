package com.kvlg.runningtracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Application class for DI
 *
 * @author Konstantin Koval
 * @since 19.07.2020
 */

@HiltAndroidApp
class RunApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}