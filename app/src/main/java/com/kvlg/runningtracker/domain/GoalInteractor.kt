package com.kvlg.runningtracker.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.kvlg.runningtracker.R
import com.kvlg.runningtracker.db.goals.GoalConverter
import com.kvlg.runningtracker.models.WeekGoal
import com.kvlg.runningtracker.repository.GoalRepository
import com.kvlg.runningtracker.repository.MainRepository
import com.kvlg.runningtracker.utils.TrackingUtils
import kotlinx.coroutines.Dispatchers
import com.kvlg.runningtracker.db.goals.WeekGoal as WeekGoalDb

/**
 * Interactor with goal repos
 *
 * @author Konstantin Koval
 * @since 13.09.2020
 */
class GoalInteractor(
    private val goalsRepository: GoalRepository,
    private val goalConverter: GoalConverter,
    private val mainRepository: MainRepository,
    private val resourceManager: ResourceManager
) {

    fun loadPeriodDistance(timeStamp: Long): LiveData<String> {
        return liveData(Dispatchers.IO) {
            val source = mainRepository.getPeriodDistance(timeStamp).map {
                resourceManager.getString(R.string.distance_placeholder, String.format("%.2f", (it?.sum() ?: 0) / 1000F))
            }
            emitSource(source)
        }
    }

    fun loadPeriodDuration(timeStamp: Long): LiveData<String> {
        return liveData(Dispatchers.IO) {
            val source = mainRepository.getPeriodDuration(timeStamp).map {
                resourceManager.getString(R.string.time_goal_placeholder, TrackingUtils.getFormattedStopWatchTime(it?.sum() ?: 0L))
            }
            emitSource(source)
        }
    }

    fun loadPeriodSpeed(timeStamp: Long): LiveData<String> {
        return liveData(Dispatchers.IO) {
            val source = mainRepository.getPeriodAvgSpeed(timeStamp).map {
                resourceManager.getString(R.string.speed_placeholder, String.format("%.2f", it?.average()?.toFloat() ?: 0F))
            }
            emitSource(source)
        }
    }

    fun loadPeriodCalories(timeStamp: Long): LiveData<String> {
        return liveData(Dispatchers.IO) {
            val source = mainRepository.getPeriodCalories(timeStamp).map {
                resourceManager.getString(R.string.calories_placeholder, (it?.sum() ?: 0).toString())
            }
            emitSource(source)
        }
    }

    fun loadPeriodPace(timeStamp: Long): LiveData<String> {
        return liveData(Dispatchers.IO) {
            val source = mainRepository.getPeriodAvgPace(timeStamp).map {
                resourceManager.getString(R.string.pace_placeholder, TrackingUtils.getFormattedPaceTime(it?.average()?.toLong() ?: 0L))
            }
            emitSource(source)
        }
    }

    fun loadGoalsFromDb(): LiveData<WeekGoal> {
        return liveData(Dispatchers.IO) {
            val source = goalsRepository.getWeekGoals().map {
                goalConverter.convertToDomainModel(it)
            }
            emitSource(source)
        }
    }


    suspend fun saveGoalsIntoDb(goal: WeekGoal) {
        goalsRepository.insertGoal(
            WeekGoalDb(
                distance = goal.distance.toDouble(),
                time = goal.time.toDouble(),
                speed = goal.speed.toDouble(),
                calories = goal.calories.toDouble()
            )
        )
    }
}