package com.kvlg.runningtracker.adapters

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.kvlg.runningtracker.R
import com.kvlg.runningtracker.databinding.RunItemBinding
import com.kvlg.runningtracker.db.Run
import com.kvlg.runningtracker.utils.TrackingUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * Viewholder for [RunAdapter]
 *
 * @author Konstantin Koval
 * @since 09.08.2020
 */
class RunViewHolder(private val runItemBinding: RunItemBinding) : RecyclerView.ViewHolder(runItemBinding.root) {
    fun bind(run: Run) {
        with(runItemBinding) {
            Glide.with(root.context).load(run.img).override(Target.SIZE_ORIGINAL).into(runImage)

            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timestamp
            }
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            dateValueTextView.text = sdf.format(calendar.time)
            avgValueSpeedTextView.text = root.context.getString(R.string.speed_placeholder, run.avgSpeedInKMH.toInt().toString())
            distanceValueTextView.text = root.context.getString(R.string.distance_placeholder, (run.distanceInMeters / 1000).toString())
            durationValueTextView.text = TrackingUtils.getFormattedStopWatchTime(run.timeInMillis)
            caloriesValueTextView.text = root.context.getString(R.string.calories_placeholder, run.caloriesBurned.toString())
        }
    }
}