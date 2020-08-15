package com.kvlg.runningtracker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.kvlg.runningtracker.databinding.RunItemBinding
import com.kvlg.runningtracker.db.Run
import com.kvlg.runningtracker.ui.viewmodels.RunsLiveDataRegistry

/**
 * Adapter for [Run] items to display
 *
 * @author Konstantin Koval
 * @since 09.08.2020
 */
class RunAdapter(
    diffCallback: RunDiffCallback,
    private val runsLiveDataRegistry: RunsLiveDataRegistry
) : ListAdapter<Run, RunViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder =
        RunViewHolder(RunItemBinding.inflate(LayoutInflater.from(parent.context)), runsLiveDataRegistry)

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    override fun onViewAttachedToWindow(holder: RunViewHolder) {
        holder.onAttach()
    }

    override fun onViewDetachedFromWindow(holder: RunViewHolder) {
        holder.onDetach()
    }
}