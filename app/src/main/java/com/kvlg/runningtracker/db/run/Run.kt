package com.kvlg.runningtracker.db.run

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data class of Run entity
 *
 * @property [img] Image with run path
 * @property [timestamp] Start time of run
 * @property [avgSpeedInKMH] Average Speed in Km/H
 * @property [distanceInMeters] Distance of one run in meters
 * @property [timeInMillis] Total time of one run in millis
 * @property [caloriesBurned] Quantity of burned calories
 * @constructor Creates an Run entity for [RunDatabase]
 *
 * @author Konstantin Koval
 * @since 19.07.2020
 */
@Entity(tableName = "run_table")
data class Run(
    var img: Bitmap? = null,
    var timestamp: Long = 0L,
    var avgSpeedInKMH: Float = 0f,
    var distanceInMeters: Int = 0,
    var timeInMillis: Long = 0L,
    var caloriesBurned: Int = 0
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}
