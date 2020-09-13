package com.kvlg.runningtracker.ui.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.kvlg.runningtracker.databinding.FragmentGoalsBinding
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding) {
            saveButton.setOnClickListener {
                profileViewModel.saveGoals(
                    distanceInput.text?.toString(),
                    durationInput.text?.toString(),
                    speedInput.text?.toString(),
                    caloriesInput.text?.toString()
                )
                requireActivity().onBackPressed()
            }
        }
    }
}