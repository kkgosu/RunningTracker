package com.kvlg.runningtracker.db.goals

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Data-access-object for [GoalsDatabase]
 *
 * @author Konstantin Koval
 * @since 12.09.2020
 */
@Dao
interface GoalsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: WeekGoal)

    @Query("SELECT * FROM goals_table WHERE id = 1")
    fun getWeekGoal(): LiveData<WeekGoal>
}