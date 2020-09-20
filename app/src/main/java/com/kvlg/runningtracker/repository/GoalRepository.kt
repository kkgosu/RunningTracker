package com.kvlg.runningtracker.repository

import com.kvlg.runningtracker.db.goals.GoalDao
import com.kvlg.runningtracker.db.goals.WeekGoal
import javax.inject.Inject

/**
 * Collects the data from [GoalDao]
 *
 * @author Konstantin Koval
 * @since 20.09.2020
 */
class GoalRepository @Inject constructor(
    private val goalsDao: GoalDao
) {

    fun getWeekGoals() = goalsDao.getWeekGoal()

    suspend fun insertGoal(goal: WeekGoal) = goalsDao.insertGoal(goal)
}