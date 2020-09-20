package com.kvlg.runningtracker.ui.main

import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.core.content.edit
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.kvlg.runningtracker.db.run.Run
import com.kvlg.runningtracker.domain.ImageLoader
import com.kvlg.runningtracker.repository.MainRepository
import com.kvlg.runningtracker.ui.fragments.common.RunsLiveDataRegistry
import com.kvlg.runningtracker.utils.Constants
import kotlinx.coroutines.launch

/**
 * @author Konstantin Koval
 * @since 19.07.2020
 */
class MainViewModel @ViewModelInject constructor(
    private val mainRepository: MainRepository,
    private val sharedPrefs: SharedPreferences,
    private val runsLiveData: RunsLiveDataRegistry,
    private val imageLoader: ImageLoader
) : ViewModel() {

    private val _savedRun = MutableLiveData<Run>()
    val savedRun: LiveData<Run> = _savedRun

    //region runs
    private val runsSortedByDate = mainRepository.getAllRunsSortedByDate()
    private val runsSortedByDistance = mainRepository.getAllRunsSortedByDistance()
    private val runsSortedByCaloriesBurned = mainRepository.getAllRunsSortedByCaloriesBurned()
    private val runsSortedByTimeInMillis = mainRepository.getAllRunsSortedByTimeInMillis()
    private val runsSortedByAvgSpeed = mainRepository.getAllRunsSortedByAvgSpeed()

    val runs = MediatorLiveData<List<Run>>()
    var sortType = SortTypes.DATE


    init {
        runs.addSourceWithSortType(runsSortedByDate, SortTypes.DATE)
        runs.addSourceWithSortType(runsSortedByAvgSpeed, SortTypes.AVG_SPEED)
        runs.addSourceWithSortType(runsSortedByDistance, SortTypes.DISTANCE)
        runs.addSourceWithSortType(runsSortedByCaloriesBurned, SortTypes.CALORIES_BURNED)
        runs.addSourceWithSortType(runsSortedByTimeInMillis, SortTypes.RUNNING_TIME)

        sortType = SortTypes.values()[sharedPrefs.getInt(Constants.KEY_PREF_SORT_TYPE, 0)]
    }

    //endregion

    fun sortRuns(sortType: SortTypes) = when (sortType) {
        SortTypes.DATE -> runsSortedByDate.applyRuns()
        SortTypes.AVG_SPEED -> runsSortedByAvgSpeed.applyRuns()
        SortTypes.DISTANCE -> runsSortedByDistance.applyRuns()
        SortTypes.CALORIES_BURNED -> runsSortedByCaloriesBurned.applyRuns()
        SortTypes.RUNNING_TIME -> runsSortedByTimeInMillis.applyRuns()
    }.also {
        this.sortType = sortType
        sharedPrefs.edit {
            putInt(Constants.KEY_PREF_SORT_TYPE, sortType.ordinal)
        }
    }

    fun saveImageOnDisk(bmp: Bitmap, run: Run) {
        viewModelScope.launch {
            val path = imageLoader.saveImageIntoDisk(bmp)
            val newRun = run.copy(imgPath = path)
            mainRepository.insertRun(newRun)
            _savedRun.postValue(newRun)
        }
    }

    fun deleteRun(id: Int) = viewModelScope.launch {
        runsLiveData.setLoadingById(id, true)
        runs.value?.get(id)?.let {
            mainRepository.deleteRun(it)
            runsLiveData.setLoadingById(id, false)
        }
    }

    private fun <T : List<Run>> LiveData<T>.applyRuns() = value?.let { runs.value = it }

    private fun <T : List<Run>> MediatorLiveData<T>.addSourceWithSortType(source: LiveData<T>, type: SortTypes) = addSource(source) { result ->
        if (sortType == type) {
            result?.let { runs.value = it }
        }
    }
}