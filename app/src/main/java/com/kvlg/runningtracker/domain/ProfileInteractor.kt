package com.kvlg.runningtracker.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.kvlg.runningtracker.db.goals.GoalConverter
import com.kvlg.runningtracker.models.WeekGoal
import com.kvlg.runningtracker.repository.MainRepository
import kotlinx.coroutines.Dispatchers
import com.kvlg.runningtracker.db.goals.WeekGoal as WeekGoalDb

/**
 * @author Konstantin Koval
 * @since 13.09.2020
 */
class ProfileInteractor(
    private val mainRepository: MainRepository,
    private val goalConverter: GoalConverter
) {

    fun loadGoalsFromDb(): LiveData<WeekGoal> {
        return liveData(Dispatchers.IO) {
            val source = mainRepository.getWeekGoals().map {
                goalConverter.convertToDomainModel(it)
            }
            emitSource(source)
        }
    }


    suspend fun saveGoalsIntoDb(goal: WeekGoal) {
        mainRepository.insertGoal(
            WeekGoalDb(
                distance = goal.distance.toDouble(),
                time = goal.time.toDouble(),
                speed = goal.speed.toDouble(),
                calories = goal.calories.toDouble()
            )
        )
    }
}