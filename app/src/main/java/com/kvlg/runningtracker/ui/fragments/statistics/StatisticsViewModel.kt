package com.kvlg.runningtracker.ui.fragments.statistics

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.kvlg.runningtracker.repository.MainRepository

/**
 * @author Konstantin Koval
 * @since 19.07.2020
 */
class StatisticsViewModel @ViewModelInject constructor(
    mainRepository: MainRepository
) : ViewModel() {

    val totalTimeRun = mainRepository.getTotalTimeInMillis()
    val totalDistance = mainRepository.getTotalDistance()
    val totalCaloriesBurned = mainRepository.getTotalCaloriesBurned()
    val totalAvgSpeed = mainRepository.getTotalAvgSpeed()

    val runsSortedByDate = mainRepository.getAllRunsSortedByDate()
}