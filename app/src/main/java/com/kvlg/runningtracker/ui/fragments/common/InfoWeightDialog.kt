package com.kvlg.runningtracker.ui.fragments.common

import android.app.Dialog
import android.os.Bundle
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kvlg.runningtracker.R

/**
 * Information dialog about weight and calories burned formula
 *
 * @author Konstantin Koval
 * @since 18.08.2020
 */
class InfoWeightDialog : DialogFragment() {

    companion object {
        const val TAG = "InfoWeightDialog"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
        .setTitle(getString(R.string.info_dialog_title))
        .setMessage(
            buildSpannedString {
                appendln("Weight needed to calculate calories burned using simple formula:")
                bold {
                    appendln("CB =((MET* WEIGHT * 3.5) / 200) * RUNNING_TIME")
                }
                append(
                    """
                    Weight in KG
                    Running time in minutes
                    MET - measurement of the energy cost of physical activity for a period of time
                    Constant value equal to 9
                """.trimIndent()
                )
            }
        )
        .setIcon(R.drawable.ic_round_info_24)
        .setPositiveButton(android.R.string.ok) { dialog, _ ->
            dialog.dismiss()
        }
        .create()

}