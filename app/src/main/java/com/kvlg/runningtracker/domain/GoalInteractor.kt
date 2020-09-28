package com.kvlg.runningtracker.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.kvlg.runningtracker.db.goals.GoalConverter
import com.kvlg.runningtracker.models.WeekGoal
import com.kvlg.runningtracker.repository.GoalRepository
import com.kvlg.runningtracker.repository.MainRepository
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
) {

    fun loadPeriodDistance(timeStamp: Long): LiveData<Float> {
        return liveData(Dispatchers.IO) {
            val source = mainRepository.getPeriodDistance(timeStamp).map { (it?.sum() ?: 0) / 1000F }
            emitSource(source)
        }
    }

    fun loadPeriodDuration(timeStamp: Long): LiveData<Long> {
        return liveData(Dispatchers.IO) {
            val source = mainRepository.getPeriodDuration(timeStamp).map { it?.sum() ?: 0L }
            emitSource(source)
        }
    }

    fun loadPeriodSpeed(timeStamp: Long): LiveData<Float> {
        return liveData(Dispatchers.IO) {
            val source = mainRepository.getPeriodAvgSpeed(timeStamp).map { it?.average()?.toFloat() ?: 0F }
            emitSource(source)
        }
    }

    fun loadPeriodCalories(timeStamp: Long): LiveData<Int> {
        return liveData(Dispatchers.IO) {
            val source = mainRepository.getPeriodCalories(timeStamp).map { it?.sum() ?: 0 }
            emitSource(source)
        }
    }

    fun loadPeriodPace(timeStamp: Long): LiveData<Long> {
        return liveData(Dispatchers.IO) {
            val source = mainRepository.getPeriodAvgPace(timeStamp).map { it?.average()?.toLong() ?: 0L }
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
                calories = goal.calories.toDouble(),
                pace = goal.pace.toDouble()
            )
        )
    }
}