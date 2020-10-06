package com.kvlg.runningtracker.domain

import android.content.Context
import com.kvlg.runningtracker.db.goals.GoalDatabase
import com.kvlg.runningtracker.db.run.RunDatabase
import java.io.File

/**
 * @author Konstantin Koval
 * @since 06.10.2020
 */
class CacheManager(
    private val context: Context,
    private val runDb: RunDatabase,
    private val goalDb: GoalDatabase
) {
    fun clearApplicationData() {
        runDb.clearAllTables()
        goalDb.clearAllTables()

        val cacheDir = context.cacheDir
        val appDir = File(cacheDir.parent)
        if (appDir.exists()) {
            appDir.list().forEach {
                if (it != "lib") deleteFile(File(appDir, it))
            }
        }
    }

    private fun deleteFile(file: File?): Boolean {
        var deleteAll = true
        file?.let { current ->
            if (current.isDirectory) {
                current.list().forEach {
                    deleteAll = deleteFile(File(current, it)) && deleteAll
                }
            } else {
                deleteAll = current.delete()
            }
        }
        return deleteAll
    }
}