package com.kvlg.runningtracker.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject


class RunsLiveDataRegistry @Inject constructor() {
    private val _loading = MutableLiveData<Pair<Int, Boolean>>()
    val isLoading: LiveData<Pair<Int, Boolean>> = _loading

    fun setLoadingById(id: Int, loading: Boolean) {
        _loading.value = Pair(id, loading)
    }
}
