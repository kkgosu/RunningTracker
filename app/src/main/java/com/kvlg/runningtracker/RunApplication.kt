package com.kvlg.runningtracker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
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

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        Timber.plant(Timber.DebugTree())
    }
}