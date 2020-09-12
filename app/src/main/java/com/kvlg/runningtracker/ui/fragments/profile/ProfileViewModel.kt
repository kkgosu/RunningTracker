package com.kvlg.runningtracker.ui.fragments.profile

import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.content.edit
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.kvlg.runningtracker.models.WeekGoal
import com.kvlg.runningtracker.models.WeekResult
import com.kvlg.runningtracker.repository.MainRepository
import com.kvlg.runningtracker.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for [ProfileFragment]
 *
 * @author Konstantin Koval
 * @since 07.09.2020
 */
class ProfileViewModel @ViewModelInject constructor(
    private val prefs: SharedPreferences,
    private val requestManager: RequestManager,
    private val mainRepository: MainRepository
) : ViewModel() {

    private val _drawable = MutableLiveData<Drawable>()
    private val _currentResults = MutableLiveData<WeekResult>()
    private val _weekGoals = MutableLiveData<WeekGoal>()

    /**
     * Avatar image drawable
     */
    val drawable: LiveData<Drawable> = _drawable

    /**
     * Current week results
     */
    val currentResults: LiveData<WeekResult> = _currentResults

    /**
     * Current week goals
     */
    val weekGoals: LiveData<WeekGoal> = _weekGoals

    fun getWeekGoal() {
        val result = mainRepository.getWeekGoals()
        result.value?.let {
            _weekGoals.value = WeekGoal(
                time = it.time,
                speed = it.speed,
                distance = it.distance,
                calories = it.calories
            )
        }
    }

    fun loadAvatar() {
        prefs.getString(Constants.KEY_PREF_AVATAR_URI, null)?.let {
            val uri = Uri.parse(it)
            viewModelScope.launch(Dispatchers.IO) {
                val drawable = requestManager.load(uri)
                    .transition(DrawableTransitionOptions.withCrossFade(375))
                    .submit()
                    .get()
                _drawable.postValue(drawable)
            }
        }
    }

    fun saveAvatar(uri: Uri) {
        prefs.edit {
            putString(Constants.KEY_PREF_AVATAR_URI, uri.toString())
        }
        loadAvatar()
    }

}