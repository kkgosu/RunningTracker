package com.kvlg.runningtracker.db.run

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

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
@TypeConverters(BitmapConverter::class)
abstract class RunDatabase : RoomDatabase() {

    /**
     * @return [RunDAO] for actions with table
     */
    abstract fun getRunDao(): RunDAO
}