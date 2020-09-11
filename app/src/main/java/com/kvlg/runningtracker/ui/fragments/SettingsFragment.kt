package com.kvlg.runningtracker.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.kvlg.runningtracker.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * @author Konstantin Koval
 * @since 26.08.2020
 */
@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding: FragmentSettingsBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.run {
                setDisplayHomeAsUpEnabled(true)
            }
            title = ""
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}