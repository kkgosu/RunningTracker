package com.kvlg.runningtracker.utils

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.LayoutRes
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.kvlg.runningtracker.R
import com.kvlg.runningtracker.databinding.MarkerViewBinding
import com.kvlg.runningtracker.db.Run
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Konstantin Koval
 * @since 12.08.2020
 */
class CustomMarkerView(
    context: Context,
    private val runs: List<Run>,
    @LayoutRes layoutResId: Int
) : MarkerView(context, layoutResId) {

    private val binding = MarkerViewBinding.inflate(LayoutInflater.from(context), this, true)

    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if (e == null) {
            return
        }
        val currentRunId = e.x.toInt()
        val run = runs[currentRunId]

        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timestamp
        }
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        binding.apply {
            dateTextView.text = sdf.format(calendar.time)
            avgSpeedTextView.text = root.context.getString(R.string.speed_placeholder, run.avgSpeedInKMH.toInt())
            distanceTextView.text = root.context.getString(R.string.distance_placeholder, run.distanceInMeters / 1000)
            timeTextView.text = TrackingUtils.getFormattedStopWatchTime(run.timeInMillis)
            caloriesBurnedTextView.text = root.context.getString(R.string.calories_placeholder, run.caloriesBurned)
        }
    }
}