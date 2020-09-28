package com.kvlg.runningtracker.models

/**
 * Week goals for profile (domain layer)
 *
 * @author Konstantin Koval
 * @since 12.09.2020
 */
data class WeekGoal(
    val time: String,
    val speed: String,
    val distance: String,
    val calories: String,
    val pace: String
)