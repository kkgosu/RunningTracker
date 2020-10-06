package com.kvlg.runningtracker.db.run

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * Data-Access-Object for [RunDatabase]
 *
 * @author Konstantin Koval
 * @since 19.07.2020
 */
@Dao
interface RunDAO {
    /**
     * Insert a run into a table
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    /**
     * Delete a run from a table
     */
    @Delete
    suspend fun deleteRun(run: Run)

    /**
     * @return [LiveData] of all runs sorted by date
     */
    @Query("SELECT * FROM run_table ORDER BY timestamp DESC")
    fun getAllRunsSortedByDate(): LiveData<List<Run>>

    /**
     * @return [LiveData] of all runs sorted by average speed
     */
    @Query("SELECT * FROM run_table ORDER BY avgSpeedInKMH DESC")
    fun getAllRunsSortedByAvgSpeed(): LiveData<List<Run>>

    /**
     * @return [LiveData] of all runs sorted by distance
     */
    @Query("SELECT * FROM run_table ORDER BY distanceInMeters DESC")
    fun getAllRunsSortedByDistance(): LiveData<List<Run>>

    /**
     * @return [LiveData] of all runs sorted by time in millis
     */
    @Query("SELECT * FROM run_table ORDER BY timeInMillis DESC")
    fun getAllRunsSortedByTimeInMillis(): LiveData<List<Run>>

    /**
     * @return [LiveData] of all runs sorted by burned calories
     */
    @Query("SELECT * FROM run_table ORDER BY caloriesBurned DESC")
    fun getAllRunsSortedByCaloriesBurned(): LiveData<List<Run>>

    /**
     * @return [LiveData] of total time in millis
     */
    @Query("SELECT SUM(timeInMillis) FROM run_table")
    fun getTotalTimeInMills(): LiveData<Long>

    /**
     * @return [LiveData] of total burned calories
     */
    @Query("SELECT SUM(caloriesBurned) FROM run_table")
    fun getTotalCaloriesBurned(): LiveData<Int>

    /**
     * @return [LiveData] of total distance
     */
    @Query("SELECT SUM(distanceInMeters) FROM run_table")
    fun getTotalDistance(): LiveData<Int>

    /**
     * @return [LiveData] of total average speed
     */
    @Query("SELECT AVG(avgSpeedInKMH) FROM run_table")
    fun getTotalAvgSpeed(): LiveData<Float>

    /**
     * @return [LiveData] of total distance in period currentTimeInMillis - [timeStamp]
     */
    @Query("SELECT distanceInMeters FROM run_table WHERE timestamp >= :timeStamp")
    fun getPeriodDistance(timeStamp: Long): LiveData<List<Int>?>

    /**
     * @return [LiveData] of total calories in period currentTimeInMillis - [timeStamp]
     */
    @Query("SELECT caloriesBurned FROM run_table WHERE timestamp >= :timeStamp")
    fun getPeriodCalories(timeStamp: Long): LiveData<List<Int>?>

    /**
     * @return [LiveData] of total running time in period currentTimeInMillis - [timeStamp]
     */
    @Query("SELECT timeInMillis FROM run_table WHERE timestamp >= :timeStamp")
    fun getPeriodDuration(timeStamp: Long): LiveData<List<Long>?>

    /**
     * @return [LiveData] of avg pace in period currentTimeInMillis - [timeStamp]
     */
    @Query("SELECT avgPaceTime FROM run_table WHERE timestamp >= :timeStamp")
    fun getPeriodAvgPace(timeStamp: Long): LiveData<List<Long>?>

    /**
     * @return [LiveData] of avg speed in period currentTimeInMillis - [timeStamp]
     */
    @Query("SELECT avgSpeedInKMH FROM run_table WHERE timestamp >= :timeStamp")
    fun getPeriodAvgSpeed(timeStamp: Long): LiveData<List<Float>?>
}