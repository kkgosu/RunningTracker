package com.kvlg.runningtracker.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kvlg.runningtracker.db.Run
import com.kvlg.runningtracker.repository.MainRepository
import kotlinx.coroutines.launch

/**
 * @author Konstantin Koval
 * @since 19.07.2020
 */
class MainViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
) : ViewModel() {

    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }
}