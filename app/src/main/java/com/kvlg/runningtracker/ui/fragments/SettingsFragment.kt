package com.kvlg.runningtracker.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.kvlg.runningtracker.R
import com.kvlg.runningtracker.databinding.FragmentSettingsBinding
import com.kvlg.runningtracker.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * @author Konstantin Koval
 * @since 19.07.2020
 */
@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadFieldsFromSharedPref()
        binding.applyChangesButton.setOnClickListener {
            val isSuccess = applyChangesToSharedPref()
            if (isSuccess) {
                Snackbar.make(view, getString(R.string.saved_settings_snackbar_text), Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(view, getString(R.string.error_save_settings_snackbar_text), Snackbar.LENGTH_LONG).show()
            }
        }

    }

    private fun loadFieldsFromSharedPref() {
        val name = sharedPreferences.getString(Constants.KEY_PREF_NAME, "") ?: ""
        val weight = sharedPreferences.getFloat(Constants.KEY_PREF_WEIGHT, 80f)
        binding.nameEditText.setText(name)
        binding.weightEditText.setText(weight.toString())
    }

    private fun applyChangesToSharedPref(): Boolean {
        val nameText = binding.nameEditText.text.toString()
        val weightText = binding.weightEditText.text.toString()
        if (nameText.isEmpty() || weightText.isEmpty()) {
            return false
        }
        sharedPreferences.edit().apply {
            putString(Constants.KEY_PREF_NAME, nameText)
            putFloat(Constants.KEY_PREF_WEIGHT, weightText.toFloat())
        }.apply()

        //val toolbarText = "Let's go $nameText!"
        return true
    }
}