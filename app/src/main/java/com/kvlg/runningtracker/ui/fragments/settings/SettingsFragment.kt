package com.kvlg.runningtracker.ui.fragments.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.kvlg.runningtracker.databinding.FragmentSettingsBinding
import com.kvlg.runningtracker.utils.BnvVisibilityListener
import com.kvlg.runningtracker.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * @author Konstantin Koval
 * @since 26.08.2020
 */
@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding: FragmentSettingsBinding
        get() = _binding!!

    @Inject
    lateinit var sharedPref: SharedPreferences

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
        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.run {
                setDisplayHomeAsUpEnabled(true)
            }
            title = ""
        }
        val name = sharedPref.getString(Constants.KEY_PREF_NAME, "Name")!!
        val email = sharedPref.getString(Constants.KEY_PREF_EMAIL, "Email")!!
        binding.nameTextView.text = name
        binding.emailTextView.text = email
        binding.accountSettingsButton.setOnClickListener {
            NameEmailDialog(name, email).apply {
                isCancelable = false
                setListener { name, email ->
                    sharedPref.edit().apply {
                        putString(Constants.KEY_PREF_NAME, name)
                        putString(Constants.KEY_PREF_EMAIL, email)
                    }.apply()
                    binding.nameTextView.text = name
                    binding.emailTextView.text = email
                }
            }.show(parentFragmentManager, NameEmailDialog.TAG)
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}