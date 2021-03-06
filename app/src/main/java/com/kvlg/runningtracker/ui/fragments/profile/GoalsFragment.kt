package com.kvlg.runningtracker.ui.fragments.profile

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.textfield.TextInputEditText
import com.kvlg.runningtracker.R
import com.kvlg.runningtracker.databinding.FragmentGoalsBinding
import com.kvlg.runningtracker.models.WeekGoal
import com.kvlg.runningtracker.ui.fragments.common.ConfirmationDialog
import com.kvlg.runningtracker.utils.BnvVisibilityListener
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment for setting and editing goals
 *
 * @author Konstantin Koval
 * @since 12.09.2020
 */
@AndroidEntryPoint
class GoalsFragment : Fragment() {
    private var _binding: FragmentGoalsBinding? = null
    private val binding: FragmentGoalsBinding get() = _binding!!

    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as BnvVisibilityListener).hide(true)
    }

    override fun onDetach() {
        (requireActivity() as BnvVisibilityListener).hide(false)
        super.onDetach()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupToolbar()
        subscribeObservers()
        if (savedInstanceState != null) {
            val cancelTrackingDialog = parentFragmentManager.findFragmentByTag(ConfirmationDialog.TAG) as? ConfirmationDialog
            cancelTrackingDialog?.setListener { closeScreen() }
        }
        binding.saveButton.setOnClickListener {
            profileViewModel.saveGoals(createWeekGoal())
            closeScreen()
        }
        profileViewModel.getWeekGoal()
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    @SuppressLint("SetTextI18n")
    private fun subscribeObservers() {
        profileViewModel.weekGoals.observe(viewLifecycleOwner) {
            with(binding) {
                distanceInput.setText(it.distance)
                speedInput.setText(it.speed)
                durationInput.setText(it.time)
                caloriesInput.setText(it.calories)
                paceInput.setText(it.pace)
            }
        }
        profileViewModel.closeScreen.observe(viewLifecycleOwner) {
            closeScreen()
        }
        profileViewModel.closeScreenAlert.observe(viewLifecycleOwner) {
            ConfirmationDialog.newInstance(R.string.are_you_sure_title, R.string.changes_wont_be_saved).apply {
                setListener { closeScreen() }
            }.show(parentFragmentManager, ConfirmationDialog.TAG)
        }
    }

    private fun setupToolbar() {
        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.run {
                setDisplayHomeAsUpEnabled(true)
                title = ""
            }
        }
        binding.toolbar.setNavigationOnClickListener {
            profileViewModel.onBackClicked(createWeekGoal())
        }
    }

    private fun closeScreen() {
        requireActivity().onBackPressed()
    }

    private fun createWeekGoal(): WeekGoal = with(binding) {
        WeekGoal(
            time = durationInput.getOrZero(),
            speed = speedInput.getOrZero(),
            distance = distanceInput.getOrZero(),
            calories = caloriesInput.getOrZero(),
            pace = paceInput.getOrZero()
        )
    }

    private fun TextInputEditText.getOrZero(): String = (text?.toString() ?: ZERO)

    companion object {
        private const val ZERO = "0"
    }
}