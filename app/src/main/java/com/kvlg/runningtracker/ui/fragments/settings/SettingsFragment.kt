package com.kvlg.runningtracker.ui.fragments.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.kvlg.runningtracker.R
import com.kvlg.runningtracker.databinding.FragmentSettingsBinding
import com.kvlg.runningtracker.utils.BnvVisibilityListener
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

    private val settingsViewModel: SettingsViewModel by viewModels()

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
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        subscribeToObservers()
        setupToolbar()
        settingsViewModel.loadValues()
        binding.accountSettingsButton.setOnClickListener { settingsViewModel.showNameEmailDialog() }
        binding.weightSettingsButton.setOnClickListener { settingsViewModel.showWeightDialog() }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    private fun setupToolbar() {
        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.run {
                setDisplayHomeAsUpEnabled(true)
            }
            title = ""
        }
    }

    private fun subscribeToObservers() {
        settingsViewModel.nameAndEmail.observe(viewLifecycleOwner) { (name, email) ->
            binding.nameTextView.text = name
            binding.emailTextView.text = email
        }
        settingsViewModel.weight.observe(viewLifecycleOwner, { binding.weightTextView.text = getString(R.string.weight_placeholder, it) })
        settingsViewModel.showNameEmailDialog.observe(viewLifecycleOwner) { (name, email) ->
            NameEmailDialog.newInstance(name, email).apply {
                isCancelable = false
                setListener { newName, newEmail -> settingsViewModel.saveNameAndEmail(newName, newEmail) }
            }.show(parentFragmentManager, NameEmailDialog.TAG)
        }
        settingsViewModel.showWeightDialog.observe(viewLifecycleOwner) {
            WeightDialog.newInstance(it).apply {
                isCancelable = false
                setListener { weight -> settingsViewModel.saveWeight(weight) }
            }.show(parentFragmentManager, WeightDialog.TAG)
        }
    }
}