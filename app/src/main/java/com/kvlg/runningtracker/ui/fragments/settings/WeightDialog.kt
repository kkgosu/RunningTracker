package com.kvlg.runningtracker.ui.fragments.settings

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kvlg.runningtracker.R
import com.kvlg.runningtracker.databinding.SettingsWeightLayoutBinding

/**
 * Dialog for weight input
 *
 * @author Konstantin Koval
 * @since 01.10.2020
 */
class WeightDialog : DialogFragment() {
    private var listener: ((String) -> Unit)? = null

    private var _binding: SettingsWeightLayoutBinding? = null
    private val binding: SettingsWeightLayoutBinding get() = _binding!!

    private var weight: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            weight = it.getString(WEIGHT_KEY)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = SettingsWeightLayoutBinding.inflate(LayoutInflater.from(requireContext()))
        binding.weightInput.setText(weight)
        return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setView(binding.root)
            .setTitle(getString(R.string.weight_dialog_title))
            .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.default_bg))
            .setPositiveButton("Save") { _, _ ->
                listener?.let { it(binding.weightInput.text?.toString() ?: "") }
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }
            .create()
    }

    fun setListener(listener: (String) -> Unit) {
        this.listener = listener
    }

    companion object {
        const val TAG = "WeightDialog"

        private const val WEIGHT_KEY = "WEIGHT_ARG"

        fun newInstance(weight: String): WeightDialog {
            val args = Bundle().apply {
                putString(WEIGHT_KEY, weight)
            }
            val fragment = WeightDialog()
            fragment.arguments = args
            return fragment
        }
    }
}