package com.kvlg.runningtracker.ui.fragments.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.features.ReturnMode
import com.kvlg.runningtracker.R
import com.kvlg.runningtracker.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment with user's profile
 *
 * @author Konstantin Koval
 * @since 26.08.2020
 */
@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding
        get() = _binding!!

    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupToolbar()
        subscribeObservers()
        profileViewModel.loadAvatar()
        profileViewModel.getWeekGoal()
        binding.settingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_settingsFragmentV2)
        }
        binding.avatarCiv.setOnClickListener {
            ImagePicker.create(this)
                .includeVideo(false)
                .returnMode(ReturnMode.ALL)
                .folderMode(true)
                .single()
                .showCamera(true)
                .enableLog(true)
                .start()
        }
        binding.editGoalsButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_goalsFragment)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            val image = ImagePicker.getFirstImageOrNull(data)
            profileViewModel.saveAvatar(image.uri)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    private fun setupToolbar() {
        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.run {
                setDisplayHomeAsUpEnabled(false)
            }
        }
    }

    private fun subscribeObservers() {
        profileViewModel.drawable.observe(viewLifecycleOwner) {
            binding.toolbarImage.setImageDrawable(it)
            binding.avatarCiv.setImageDrawable(it)
        }
        profileViewModel.weekGoals.observe(viewLifecycleOwner) {
            with(binding) {
                timeGoalTextView.text = getString(R.string.time_goal_placeholder, it.time)
                paceGoalTextView.text = getString(R.string.pace_placeholder, it.pace)
                speedGoalTextView.text = getString(R.string.speed_placeholder, it.speed)
                caloriesGoalTextView.text = getString(R.string.calories_placeholder, it.calories)
            }
            profileViewModel.getPeriodResults(System.currentTimeMillis(), it)
        }
        with(profileViewModel) {
            periodDistance.observe(viewLifecycleOwner, binding.titleDistanceValueTextView::setText)
            periodDuration.observe(viewLifecycleOwner, binding.timeCurrentGoalTextView::setText)
            periodSpeed.observe(viewLifecycleOwner, binding.speedCurrentGoalTextView::setText)
            periodCalories.observe(viewLifecycleOwner, binding.caloriesCurrentGoalTextView::setText)
            periodPace.observe(viewLifecycleOwner, binding.paceCurrentGoalTextView::setText)
            durationProgress.observe(viewLifecycleOwner, binding.timeGoalProgress::setProgress)
            speedProgress.observe(viewLifecycleOwner, binding.speedGoalProgress::setProgress)
            caloriesProgress.observe(viewLifecycleOwner, binding.caloriesGoalProgress::setProgress)
            paceProgress.observe(viewLifecycleOwner, binding.paceGoalProgress::setProgress)
        }
    }
}
