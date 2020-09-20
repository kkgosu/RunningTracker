package com.kvlg.runningtracker.models.domain

import androidx.lifecycle.LiveData

/**
 * Selected period results
 *
 * @author Konstantin Koval
 * @since 12.09.2020
 */
data class PeriodResult(
    val duration: LiveData<Long>,
    val speed: LiveData<Float>,
    val distance: LiveData<Float>,
    val calories: LiveData<Int>,
    val pace: LiveData<Long>
)