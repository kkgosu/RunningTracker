package com.kvlg.runningtracker.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.kvlg.runningtracker.databinding.FragmentStatisticsBinding
import com.kvlg.runningtracker.ui.viewmodels.StatisticsViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * @author Konstantin Koval
 * @since 19.07.2020
 */
@AndroidEntryPoint
class StatisticFragment : Fragment() {
    private val viewModel: StatisticsViewModel by viewModels()

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }
}