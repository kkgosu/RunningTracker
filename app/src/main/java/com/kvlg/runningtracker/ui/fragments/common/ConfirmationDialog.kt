package com.kvlg.runningtracker.ui.fragments.common

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kvlg.runningtracker.R

/**
 * Simple dialog with confirmation yes/no
 *
 * @author Konstantin Koval
 * @since 13.08.2020
 */
class ConfirmationDialog : DialogFragment() {

    private var listener: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
        .setTitle(getString(arguments?.getInt(TITLE_ARG) ?: 0))
        .setMessage(getString(arguments?.getInt(MESSAGE_ARG) ?: 0))
        .setPositiveButton(getString(R.string.yes)) { _, _ ->
            listener?.let { it() }
        }
        .setNegativeButton(getString(R.string.no)) { dialog, _ ->
            dialog.cancel()
        }
        .create()

    fun setListener(listener: () -> Unit) {
        this.listener = listener
    }

    companion object {
        const val TAG = "ConfirmationDialog"

        private const val TITLE_ARG = "TITLE_ARG"
        private const val MESSAGE_ARG = "MESSAGE_ARG"

        fun newInstance(@StringRes titleResId: Int, @StringRes messageResId: Int): ConfirmationDialog {
            val args = Bundle().apply {
                putInt(TITLE_ARG, titleResId)
                putInt(MESSAGE_ARG, messageResId)
            }
            val fragment = ConfirmationDialog()
            fragment.arguments = args
            return fragment
        }
    }

}
