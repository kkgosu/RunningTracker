package com.kvlg.runningtracker.domain

import android.content.Context
import androidx.annotation.StringRes

/**
 * Helper for getting resources
 *
 * @author Konstantin Koval
 * @since 20.09.2020
 */
class ResourceManager(
    private val context: Context
) {
    @JvmName("getString")
    fun getString(@StringRes id: Int): String = context.resources.getString(id)

    @JvmName("getStringWithArgs")
    fun getString(@StringRes id: Int, vararg args: Any): String =
        context.resources.getString(id, *args)
}