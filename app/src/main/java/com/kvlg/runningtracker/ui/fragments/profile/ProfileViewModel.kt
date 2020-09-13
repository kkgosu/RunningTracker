package com.kvlg.runningtracker.ui.fragments.profile

import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.content.edit
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.kvlg.runningtracker.domain.ProfileInteractor
import com.kvlg.runningtracker.models.WeekGoal
import com.kvlg.runningtracker.models.WeekResult
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
    private val profileInteractor: ProfileInteractor
) : ViewModel() {

    private val _drawable = MutableLiveData<Drawable>()
    private val _currentResults = MutableLiveData<WeekResult>()
    private val _weekGoals = MutableLiveData<Boolean>()

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
    val weekGoals: LiveData<WeekGoal> = Transformations.switchMap(_weekGoals) {
        profileInteractor.loadGoalsFromDb()
    }

    fun getWeekGoal() {
        _weekGoals.value = true
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

    fun saveGoals(distance: String?, duration: String?, speed: String?, calories: String?) {
        viewModelScope.launch {
            profileInteractor.saveGoalsIntoDb(
                WeekGoal(
                    time = duration.getOrZero(),
                    speed = speed.getOrZero().toBigDecimal(),
                    distance = distance.getOrZero().toBigDecimal(),
                    calories = calories.getOrZero().toBigDecimal()
                )
            )
        }
    }

    private fun <T : String?> T.getOrZero(): String = if (isNullOrEmpty()) ZERO else this

    companion object {
        private const val ZERO = "0"
    }

}