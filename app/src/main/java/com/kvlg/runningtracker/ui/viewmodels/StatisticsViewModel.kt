package com.kvlg.runningtracker.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.kvlg.runningtracker.repository.MainRepository

/**
 * @author Konstantin Koval
 * @since 19.07.2020
 */
class StatisticsViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
) : ViewModel() {
}