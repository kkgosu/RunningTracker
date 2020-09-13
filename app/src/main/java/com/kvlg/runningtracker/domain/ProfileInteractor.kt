package com.kvlg.runningtracker.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.kvlg.runningtracker.models.WeekGoal
import com.kvlg.runningtracker.repository.MainRepository
import kotlinx.coroutines.Dispatchers
import java.math.BigDecimal
import com.kvlg.runningtracker.db.goals.WeekGoal as WeekGoalDb

/**
 * @author Konstantin Koval
 * @since 13.09.2020
 */
class ProfileInteractor(
    private val mainRepository: MainRepository
) {

    fun loadGoalsFromDb(): LiveData<WeekGoal> {
        return liveData(Dispatchers.IO) {
            val source = mainRepository.getWeekGoals().map {
                WeekGoal(
                    time = it.time,
                    speed = BigDecimal.valueOf(it.speed).stripTrailingZeros(),
                    distance = BigDecimal.valueOf(it.distance).stripTrailingZeros(),
                    calories = BigDecimal.valueOf(it.calories).stripTrailingZeros()
                )
            }
            emitSource(source)
        }
    }


    suspend fun saveGoalsIntoDb(goal: WeekGoal) {
        mainRepository.insertGoal(
            WeekGoalDb(
                distance = goal.distance.toDouble(),
                time = goal.time,
                speed = goal.speed.toDouble(),
                calories = goal.calories.toDouble()
            )
        )
    }
}