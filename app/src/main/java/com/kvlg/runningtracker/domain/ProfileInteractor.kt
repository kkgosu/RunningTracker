package com.kvlg.runningtracker.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.kvlg.runningtracker.models.WeekGoal
import com.kvlg.runningtracker.repository.MainRepository
import dagger.hilt.android.AndroidEntryPoint

/**
 * @author Konstantin Koval
 * @since 13.09.2020
 */
class ProfileInteractor(
    private val mainRepository: MainRepository
) {

    fun loadGoalsFromDb(): LiveData<WeekGoal>? {
        val result = mainRepository.getWeekGoals()
        return result?.let {
            Transformations.map(result) {
                WeekGoal(
                    time = it.time,
                    speed = it.speed,
                    distance = it.distance,
                    calories = it.calories
                )
            }
        }
    }

    suspend fun saveGoalsIntoDb(goal: WeekGoal) {
        mainRepository.insertGoal(
            com.kvlg.runningtracker.db.goals.WeekGoal(
                distance = goal.distance,
                time = goal.time,
                speed = goal.speed,
                calories = goal.calories
            )
        )
    }
}