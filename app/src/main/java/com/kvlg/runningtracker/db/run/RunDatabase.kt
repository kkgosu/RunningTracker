package com.kvlg.runningtracker.db.run

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kvlg.runningtracker.db.Converter

/**
 * Database class
 *
 * @author Konstantin Koval
 * @since 19.07.2020
 */

@Database(
    entities = [Run::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converter::class)
abstract class RunDatabase : RoomDatabase() {

    /**
     * @return [RunDAO] for actions with table
     */
    abstract fun getRunDao(): RunDAO
}