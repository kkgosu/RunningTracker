package com.kvlg.runningtracker.ui.fragments.common

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kvlg.runningtracker.R

/**
 * @author Konstantin Koval
 * @since 13.08.2020
 */
class CancelTrackingDialog : DialogFragment() {

    private var listener: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
        .setTitle(getString(R.string.cancel_run_alert_title))
        .setMessage(getString(R.string.cancel_run_alert_desc))
        .setIcon(R.drawable.ic_delete_24)
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
