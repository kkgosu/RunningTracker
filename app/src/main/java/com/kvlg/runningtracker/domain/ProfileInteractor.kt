package com.kvlg.runningtracker.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kvlg.runningtracker.models.WeekGoal
import com.kvlg.runningtracker.repository.MainRepository
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
        val result = mainRepository.getWeekGoals()
        val r = result.value ?: WeekGoalDb(
            time = "0h",
            speed = 0.0,
            distance = 0.0,
            calories = 0.0
        )
        return MutableLiveData(
            WeekGoal(
                time = r.time,
                speed = BigDecimal.valueOf(r.speed).stripTrailingZeros(),
                distance = BigDecimal.valueOf(r.distance).stripTrailingZeros(),
                calories = BigDecimal.valueOf(r.calories).stripTrailingZeros()
            )
        )
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