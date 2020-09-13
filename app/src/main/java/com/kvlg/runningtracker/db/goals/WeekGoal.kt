package com.kvlg.runningtracker.db.goals

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author Konstantin Koval
 * @since 12.09.2020
 */
@Entity(tableName = "goals_table")
data class WeekGoal(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1,
    val distance: Double,
    val time: Double,
    val speed: Double,
    val calories: Double
)