package com.kvlg.runningtracker.models

/**
 * Current week results
 *
 * @author Konstantin Koval
 * @since 12.09.2020
 */
data class WeekResult(
    val time: String,
    val speed: Double,
    val distance: Double,
    val calories: Double
)