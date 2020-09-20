package com.kvlg.runningtracker.db.goals

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Database for store run goals
 *
 * @author Konstantin Koval
 * @since 12.09.2020
 */
@Database(
    entities = [WeekGoal::class],
    version = 2,
    exportSchema = false
)
abstract class GoalDatabase : RoomDatabase() {

    /**
     * Get goals DAO
     */
    abstract fun getGoalsDao(): GoalDao
}