package com.kvlg.runningtracker.ui.viewmodels

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.kvlg.runningtracker.db.Run
import com.kvlg.runningtracker.repository.MainRepository
import com.kvlg.runningtracker.utils.Constants
import com.kvlg.runningtracker.utils.SortTypes
import kotlinx.coroutines.launch

/**
 * @author Konstantin Koval
 * @since 19.07.2020
 */
class MainViewModel @ViewModelInject constructor(
    private val mainRepository: MainRepository,
    private val sharedPrefs: SharedPreferences
) : ViewModel() {

    //region runs
    private val runsSortedByDate = mainRepository.getAllRunsSortedByDate()
    private val runsSortedByDistance = mainRepository.getAllRunsSortedByDistance()
    private val runsSortedByCaloriesBurned = mainRepository.getAllRunsSortedByCaloriesBurned()
    private val runsSortedByTimeInMillis = mainRepository.getAllRunsSortedByTimeInMillis()
    private val runsSortedByAvgSpeed = mainRepository.getAllRunsSortedByAvgSpeed()

    val runs = MediatorLiveData<List<Run>>()
    var sortType = SortTypes.DATE

    //endregion

    private val _toolbarTitle = MutableLiveData<String>()
    val toolbarTitle: LiveData<String> = _toolbarTitle

    init {
        runs.addSourceWithSortType(runsSortedByDate, SortTypes.DATE)
        runs.addSourceWithSortType(runsSortedByAvgSpeed, SortTypes.AVG_SPEED)
        runs.addSourceWithSortType(runsSortedByDistance, SortTypes.DISTANCE)
        runs.addSourceWithSortType(runsSortedByCaloriesBurned, SortTypes.CALORIES_BURNED)
        runs.addSourceWithSortType(runsSortedByTimeInMillis, SortTypes.RUNNING_TIME)

        sortType = SortTypes.values()[sharedPrefs.getInt(Constants.KEY_PREF_SORT_TYPE, 0)]
    }

    fun sortRuns(sortType: SortTypes) = when (sortType) {
        SortTypes.DATE -> runsSortedByDate.value?.let { runs.value = it }
        SortTypes.AVG_SPEED -> runsSortedByAvgSpeed.value?.let { runs.value = it }
        SortTypes.DISTANCE -> runsSortedByDistance.value?.let { runs.value = it }
        SortTypes.CALORIES_BURNED -> runsSortedByCaloriesBurned.value?.let { runs.value = it }
        SortTypes.RUNNING_TIME -> runsSortedByTimeInMillis.value?.let { runs.value = it }
    }.also {
        this.sortType = sortType
        sharedPrefs.edit {
            putInt(Constants.KEY_PREF_SORT_TYPE, sortType.ordinal)
        }
    }


    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }

    fun setupToolbarTitle(title: String) {

    }

    private fun <T : List<Run>> MediatorLiveData<T>.addSourceWithSortType(source: LiveData<T>, type: SortTypes) = addSource(source) { result ->
        if (sortType == type) {
            result?.let { runs.value = it }
        }
    }
}