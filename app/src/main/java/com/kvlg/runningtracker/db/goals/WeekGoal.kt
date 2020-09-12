package com.kvlg.runningtracker.db.goals

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author Konstantin Koval
 * @since 12.09.2020
 */
@Entity(tableName = "goals_table")
data class WeekGoal(
    val distance: Double = 0.0,
    val time: String = "0h",
    val speed: Double = 0.0,
    val calories: Double = 0.0
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}