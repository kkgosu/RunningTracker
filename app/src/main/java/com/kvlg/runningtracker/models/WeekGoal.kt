package com.kvlg.runningtracker.models

/**
 * Week goals for profile
 *
 * @author Konstantin Koval
 * @since 12.09.2020
 */
data class WeekGoal(
    val time: String,
    val speed: Double,
    val distance: Double,
    val calories: Double
)