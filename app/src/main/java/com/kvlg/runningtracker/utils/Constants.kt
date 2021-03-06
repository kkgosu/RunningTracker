package com.kvlg.runningtracker.utils

import android.graphics.Color

/**
 * Class which contains all global constants
 *
 * @author Konstantin Koval
 * @since 19.07.2020
 */
object Constants {
    const val RUNNING_DATABASE_NAME = "running_db"
    const val GOALS_DATABASE_NAME = "goals_db"

    const val REQUEST_CODE_LOCATION_PERMISSION = 1001

    const val MAIN_ACTIVITY_PENDING_INTENT = 0
    const val TRACKING_SERVICE_PAUSE_PENDING_INTENT = 1
    const val TRACKING_SERVICE_START_OR_RESUME_PENDING_INTENT = 2

    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val ACTION_CLEAR_VALUE_SERVICE = "ACTION_CLEAR_VALUE_SERVICE"
    const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"

    const val UPDATE_LOCATION_INTERVAL = 5000L
    const val FASTEST_UPDATE_LOCATION_INTERVAL = 2000L

    const val TIMER_UPDATE_INTERVAL = 50L

    const val POLYLINE_COLOR = Color.RED
    const val POLYLINE_WIDTH = 8f
    const val MAP_ZOOM = 17f

    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1

    const val SHARED_PREF_NAME = "sharedPref"
    const val KEY_PREF_FIRST_TIME_TOGGLE = "KEY_FIRST_TIME_TOGGLE"
    const val KEY_PREF_NAME = "KEY_PREF_NAME"
    const val KEY_PREF_EMAIL = "KEY_PREF_EMAIL"
    const val KEY_PREF_WEIGHT = "KEY_PREF_WEIGHT_TOGGLE"
    const val KEY_PREF_SORT_TYPE = "KEY_PREF_SORT_TYPE"

    const val KEY_PREF_AVATAR_URI = "KEY_PREF_AVATAR_URI"
}