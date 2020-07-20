package com.kvlg.runningtracker.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.kvlg.runningtracker.databinding.FragmentRunBinding
import com.kvlg.runningtracker.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * @author Konstantin Koval
 * @since 19.07.2020
 */
@AndroidEntryPoint
class RunFragment : Fragment() {
    private val viewModel: MainViewModel by viewModels()

    private var _binding: FragmentRunBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRunBinding.inflate(inflater, container, false)
        return binding.root
    }
}