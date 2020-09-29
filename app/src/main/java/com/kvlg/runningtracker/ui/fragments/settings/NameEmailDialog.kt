package com.kvlg.runningtracker.ui.fragments.settings

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kvlg.runningtracker.R
import com.kvlg.runningtracker.databinding.SettingsNameDialogLayoutBinding

/**
 * Dialog for input and saving user's name and email
 *
 * @author Konstantin Koval
 * @since 29.09.2020
 */
class NameEmailDialog(
    private val name: String,
    private val email: String
) : DialogFragment() {
    private var listener: ((String?, String?) -> Unit)? = null

    private var _binding: SettingsNameDialogLayoutBinding? = null
    private val binding: SettingsNameDialogLayoutBinding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = SettingsNameDialogLayoutBinding.inflate(LayoutInflater.from(requireContext()))
        with(binding) {
            nameInput.setText(name)
            emailInput.setText(email)
        }
        return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setView(binding.root)
            .setTitle(getString(R.string.name_email_title))
            .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.default_bg))
            .setPositiveButton("Save") { _, _ ->
                listener?.let { it(binding.nameInput.text?.toString() ?: "", binding.emailInput.text?.toString() ?: "") }
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }
            .create()
    }

    fun setListener(listener: (String?, String?) -> Unit) {
        this.listener = listener
    }

    companion object {
        const val TAG = "NameEmailDialog"
    }
}