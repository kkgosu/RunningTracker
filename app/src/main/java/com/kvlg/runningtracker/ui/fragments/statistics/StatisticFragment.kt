package com.kvlg.runningtracker.ui.fragments.statistics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.kvlg.runningtracker.R
import com.kvlg.runningtracker.databinding.FragmentStatisticsBinding
import com.kvlg.runningtracker.db.Run
import com.kvlg.runningtracker.utils.TrackingUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.round

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            totalDistanceValueTextView.text = getString(R.string.distance_placeholder, 0.toString())
            totalCaloriesValueTextView.text = getString(R.string.calories_placeholder, 0.toString())
            totalDistanceValueTextView.text = getString(R.string.speed_placeholder, 0.toString())
        }
        subscribeToObservers()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun subscribeToObservers() {
        viewModel.totalTimeRun.observe(viewLifecycleOwner) {
            it?.let {
                val totalTimeRun = TrackingUtils.getFormattedStopWatchTime(it)
                binding.totalTimeValueTextView.text = totalTimeRun
            }
        }

        viewModel.totalDistance.observe(viewLifecycleOwner) {
            it?.let {
                val km = it / 1000f
                val totalDistance = (round(km * 10f) / 10f).toString()
                val totalDistanceString = getString(R.string.distance_placeholder, totalDistance)
                binding.totalDistanceValueTextView.text = totalDistanceString
            }
        }

        //TODO: deal with infinity avg speed
        viewModel.totalAvgSpeed.observe(viewLifecycleOwner) {
            it?.let {
                val avgSpeed = (round(it * 10f) / 10f).toString()
                binding.avgSpeedValueTextView.text = getString(R.string.speed_placeholder, avgSpeed)
            }
        }

        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner) {
            it?.let {
                binding.totalCaloriesValueTextView.text = getString(R.string.calories_placeholder, it.toString())
            }
        }

        viewModel.runsSortedByDate.observe(viewLifecycleOwner) {
            it?.let {
                val reversedRuns = it.asReversed()
                val allAvgSpeeds = reversedRuns.indices.map { i -> BarEntry(i.toFloat(), reversedRuns[i].avgSpeedInKMH) }
                val barDataSet = BarDataSet(allAvgSpeeds, "Avg Speed Over Time").apply {
                    valueTextColor = Color.YELLOW
                    valueTextSize = CHART_TEXT_SIZE
                    color = ContextCompat.getColor(requireContext(), R.color.colorAccent)
                }
                setupBarChart(reversedRuns)
                binding.barChart.apply {
                    data = BarData(barDataSet)
                    marker = CustomMarkerView(requireContext(), reversedRuns, R.layout.marker_view)
                    animateY(500)
                    invalidate()
                }
            }
        }
    }

    private fun setupBarChart(list: List<Run>) {
        binding.barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            axisLineColor = CHART_AXIS_LINE_COLOR
            textColor = CHART_TEXT_COLOR
            textSize = CHART_TEXT_SIZE
            valueFormatter = DateValueFormatter(list)
            setDrawLabels(true)
            setDrawGridLines(false)
        }
        binding.barChart.axisLeft.apply {
            axisLineColor = CHART_AXIS_LINE_COLOR
            textColor = CHART_TEXT_COLOR
            textSize = CHART_TEXT_SIZE
            setDrawGridLines(false)
        }
        binding.barChart.axisRight.apply {
            axisLineColor = CHART_AXIS_LINE_COLOR
            textColor = CHART_TEXT_COLOR
            textSize = CHART_TEXT_SIZE
            setDrawGridLines(false)
        }
        binding.barChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawValueAboveBar(true)
            setMaxVisibleValueCount(30)
        }
    }

    companion object {
        private const val CHART_TEXT_SIZE = 16F
        private const val CHART_TEXT_COLOR = Color.WHITE
        private const val CHART_AXIS_LINE_COLOR = Color.YELLOW
    }

}