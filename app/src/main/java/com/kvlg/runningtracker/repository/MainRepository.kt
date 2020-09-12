package com.kvlg.runningtracker.repository

import androidx.lifecycle.LiveData
import com.kvlg.runningtracker.db.goals.GoalsDao
import com.kvlg.runningtracker.db.goals.WeekGoal
import com.kvlg.runningtracker.db.run.Run
import com.kvlg.runningtracker.db.run.RunDAO
import javax.inject.Inject

/**
 * Collects the data from [RunDAO]
 *
 * @author Konstantin Koval
 * @since 19.07.2020
 */
class MainRepository @Inject constructor(
    private val runDAO: RunDAO,
    private val goalsDao: GoalsDao
) {

    suspend fun insertRun(run: Run) = runDAO.insertRun(run)

    suspend fun deleteRun(run: Run) = runDAO.deleteRun(run)

    fun getAllRunsSortedByDate(): LiveData<List<Run>> = runDAO.getAllRunsSortedByDate()

    fun getAllRunsSortedByDistance(): LiveData<List<Run>> = runDAO.getAllRunsSortedByDistance()

    fun getAllRunsSortedByTimeInMillis(): LiveData<List<Run>> = runDAO.getAllRunsSortedByTimeInMillis()

    fun getAllRunsSortedByAvgSpeed(): LiveData<List<Run>> = runDAO.getAllRunsSortedByAvgSpeed()

    fun getAllRunsSortedByCaloriesBurned(): LiveData<List<Run>> = runDAO.getAllRunsSortedByCaloriesBurned()

    fun getTotalAvgSpeed() = runDAO.getTotalAvgSpeed()

    fun getTotalDistance() = runDAO.getTotalDistance()

    fun getTotalCaloriesBurned() = runDAO.getTotalCaloriesBurned()

    fun getTotalTimeInMillis() = runDAO.getTotalTimeInMills()

    fun getWeekGoals() = goalsDao.getWeekGoal()

    suspend fun insertGoal(goal: WeekGoal) = goalsDao.insertGoal(goal)
}