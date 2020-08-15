package com.kvlg.runningtracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * LiveData registry for [RunFragment]
 *
 * @author Konstantin Koval
 * @since 15.08.2020
 */
class RunsLiveDataRegistry {
    private val _loading = MutableLiveData<Pair<Int, Boolean>>()
    val loading: LiveData<Pair<Int, Boolean>> = _loading

    fun setLoadingForItem(id: Int, isLoading: Boolean) {
        _loading.value = Pair(id, isLoading)
    }
}