package com.kvlg.runningtracker.models.presentation

/**
 * Presentation model of selected period result
 *
 * @author Konstantin Koval
 * @since 20.09.2020
 */
data class PeriodResult(
    val duration: String,
    val pace: String,
    val speed: String,
    val calories: String,
    val distance: String
)