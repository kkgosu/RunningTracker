package com.kvlg.runningtracker.adapters

import androidx.recyclerview.widget.DiffUtil
import com.kvlg.runningtracker.db.Run

/**
 * [DiffUtil.ItemCallback] for [RunAdapter]
 *
 * @author Konstantin Koval
 * @since 09.08.2020
 */
class RunDiffCallback : DiffUtil.ItemCallback<Run>() {
    override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean = oldItem == newItem
}