package com.kvlg.runningtracker.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.kvlg.runningtracker.R
import com.kvlg.runningtracker.databinding.FragmentSetupBinding
import com.kvlg.runningtracker.ui.main.MainViewModel
import com.kvlg.runningtracker.utils.BnvVisibilityListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * @author Konstantin Koval
 * @since 19.07.2020
 */
@AndroidEntryPoint
class SetupFragment : Fragment() {
    private var _binding: FragmentSetupBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var savedStateHandle: SavedStateHandle

    @Inject
    lateinit var sharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity() as BnvVisibilityListener).hide(true)
        savedStateHandle = findNavController().previousBackStackEntry!!.savedStateHandle
        savedStateHandle.set(LOGIN_SUCCESSFUL, false)
        binding.continueButton.setOnClickListener {
            login()
        }
    }

    private fun login() {
        mainViewModel.writePersonalDataToSharedPref(
            binding.nameEditText.text.toString(),
            binding.weightEditText.text.toString(),
            binding.emailInput.text.toString()
        ).observe(viewLifecycleOwner) {
            if (it) {
                savedStateHandle.set(LOGIN_SUCCESSFUL, true)
                findNavController().popBackStack()
            } else {
                Snackbar.make(requireView(), getString(R.string.error_save_settings_snackbar_text), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    companion object {
        const val LOGIN_SUCCESSFUL = "LOGIN_SUCCESSFUL"
    }
}