package com.kvlg.runningtracker.ui.fragments.statistics

import com.github.mikephil.charting.formatter.ValueFormatter
import com.kvlg.runningtracker.db.Run
import java.text.SimpleDateFormat
import java.util.*

/**
 * Value formatter for date
 *
 * @author Konstantin Koval
 * @since 08.09.2020
 */
class DateValueFormatter(private val runs: List<Run>) : ValueFormatter() {
    private val sdf = SimpleDateFormat("dd.MM", Locale.getDefault())

    override fun getFormattedValue(value: Float): String = sdf.format(Date(runs[value.toInt()].timestamp))
}