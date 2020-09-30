package com.kvlg.runningtracker.ui.fragments.settings

import android.content.SharedPreferences
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kvlg.runningtracker.utils.Constants
import com.kvlg.runningtracker.utils.SingleLiveEvent

/**
 * @author Konstantin Koval
 * @since 30.09.2020
 */
class SettingsViewModel @ViewModelInject constructor(
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _nameAndEmail = MutableLiveData<Pair<String, String>>()
    private val _showNameEmailDialog = SingleLiveEvent<Pair<String, String>>()

    val nameAndEmail: LiveData<Pair<String, String>> = _nameAndEmail
    val showNameEmailDialog: LiveData<Pair<String, String>> = _showNameEmailDialog

    fun loadNameAndEmail() {
        _nameAndEmail.value = getNameEmail()
    }

    fun saveNameAndEmail(name: String, email: String) {
        _nameAndEmail.value = Pair(name, email)
        sharedPreferences.edit().apply {
            putString(Constants.KEY_PREF_NAME, name)
            putString(Constants.KEY_PREF_EMAIL, email)
        }.apply()
    }

    fun showNameEmailDialog() {
        _showNameEmailDialog.value = getNameEmail()
    }

    private fun getNameEmail(): Pair<String, String> {
        val name = sharedPreferences.getString(Constants.KEY_PREF_NAME, "Name")!!
        val email = sharedPreferences.getString(Constants.KEY_PREF_EMAIL, "Email")!!
        return Pair(name, email)
    }
}