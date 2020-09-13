package com.kvlg.runningtracker.ui.fragments.common

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kvlg.runningtracker.R

/**
 * @author Konstantin Koval
 * @since 13.08.2020
 */
class ConfirmationDialog(
    @StringRes private val titleResId: Int,
    @StringRes private val messageResId: Int
) : DialogFragment() {

    private var listener: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
        .setTitle(getString(titleResId))
        .setMessage(getString(messageResId))
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

}
