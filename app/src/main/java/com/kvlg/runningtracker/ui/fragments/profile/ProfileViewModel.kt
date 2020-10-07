package com.kvlg.runningtracker.ui.fragments.profile

import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.content.edit
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.kvlg.runningtracker.R
import com.kvlg.runningtracker.domain.GoalInteractor
import com.kvlg.runningtracker.domain.ResourceManager
import com.kvlg.runningtracker.models.WeekGoal
import com.kvlg.runningtracker.utils.Constants
import com.kvlg.runningtracker.utils.SingleLiveEvent
import com.kvlg.runningtracker.utils.TrackingUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal

/**
 * ViewModel for [ProfileFragment]
 *
 * @author Konstantin Koval
 * @since 07.09.2020
 */
class ProfileViewModel @ViewModelInject constructor(
    private val prefs: SharedPreferences,
    private val requestManager: RequestManager,
    private val goalInteractor: GoalInteractor,
    private val resourceManager: ResourceManager
) : ViewModel() {

    private val _drawable = MutableLiveData<Drawable>()
    private val _weekGoals = MutableLiveData<Boolean>()
    private val _closeScreen = SingleLiveEvent<Unit>()
    private val _closeScreenAlert = SingleLiveEvent<Unit>()
    private val _periodTrigger = MutableLiveData<Pair<Long, WeekGoal>>()
    private val _durationProgress = MutableLiveData<Float>()
    private val _paceProgress = MutableLiveData<Float>()
    private val _caloriesProgress = MutableLiveData<Float>()
    private val _speedProgress = MutableLiveData<Float>()

    /**
     * Duration progress (0-100)
     */
    val durationProgress: LiveData<Float> = _durationProgress

    /**
     * Pace progress (0-100)
     */
    val paceProgress: LiveData<Float> = _paceProgress

    /**
     * Calories progress (0-100)
     */
    val caloriesProgress: LiveData<Float> = _caloriesProgress

    /**
     * Speed progress (0-100)
     */
    val speedProgress: LiveData<Float> = _speedProgress

    /**
     * Avatar image drawable
     */
    val drawable: LiveData<Drawable> = _drawable

    /**
     * Current week goals
     */
    val weekGoals: LiveData<WeekGoal> = Transformations.switchMap(_weekGoals) {
        goalInteractor.loadGoalsFromDb()
    }

    /**
     * Close goals screen
     */
    val closeScreen: LiveData<Unit> = _closeScreen

    /**
     * Show close screen alert
     */
    val closeScreenAlert: LiveData<Unit> = _closeScreenAlert

    /**
     * Current period distance
     */
    val periodDistance: LiveData<String> = Transformations.switchMap(_periodTrigger) { (time, weekGoal) ->
        goalInteractor.loadPeriodDistance(time).map { distance ->
            resourceManager.getString(R.string.distance_placeholder, String.format("%.2f", distance))
        }
    }

    /**
     * Current period duration
     */
    val periodDuration: LiveData<String> = Transformations.switchMap(_periodTrigger) { (time, weekGoal) ->
        goalInteractor.loadPeriodDuration(time).map { duration ->
            val goal = weekGoal.time.toBigDecimal()
            _durationProgress.value = duration progressOf goal
            resourceManager.getString(R.string.time_goal_placeholder, TrackingUtils.getFormattedStopWatchTime(duration))
        }
    }

    /**
     * Current period speed
     */
    val periodSpeed: LiveData<String> = Transformations.switchMap(_periodTrigger) { (time, weekGoal) ->
        goalInteractor.loadPeriodSpeed(time).map { speed ->
            val goal = weekGoal.speed.toBigDecimal()
            _speedProgress.value = speed progressOf goal
            resourceManager.getString(R.string.speed_placeholder, String.format("%.2f", speed))
        }
    }

    /**
     * Current period calories
     */
    val periodCalories: LiveData<String> = Transformations.switchMap(_periodTrigger) { (time, weekGoal) ->
        goalInteractor.loadPeriodCalories(time).map { calories ->
            val goal = weekGoal.calories.toBigDecimal()
            _caloriesProgress.value = calories progressOf goal
            resourceManager.getString(R.string.calories_placeholder, calories.toString())
        }
    }

    /**
     * Current period pace
     */
    val periodPace: LiveData<String> = Transformations.switchMap(_periodTrigger) { (time, weekGoal) ->
        goalInteractor.loadPeriodPace(time).map { pace ->
/*            val goal = weekGoal.pace.toBigDecimal()
            _paceProgress.value = pace progressOf goal*/
            resourceManager.getString(R.string.pace_placeholder, TrackingUtils.getFormattedPaceTime(pace))
        }
    }

    fun getPeriodResults(currentTime: Long, weekGoal: WeekGoal) {
        _periodTrigger.value = Pair(currentTime - MS_IN_WEEK, weekGoal)
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

    fun saveGoals(goal: WeekGoal) {
        viewModelScope.launch {
            goalInteractor.saveGoalsIntoDb(goal)
        }
    }

    fun onBackClicked(goal: WeekGoal) {
        val oldGoal = weekGoals.value
        if (goal == oldGoal) {
            _closeScreen.call()
        } else {
            _closeScreenAlert.call()
        }
    }

    private infix fun Number.progressOf(num: BigDecimal): Float =
        (if (num.compareTo(BigDecimal.ZERO) == 0) 0F else this.toFloat() / num.toFloat()) * 100

    companion object {
        private const val MS_IN_WEEK = 604800000L
    }
}