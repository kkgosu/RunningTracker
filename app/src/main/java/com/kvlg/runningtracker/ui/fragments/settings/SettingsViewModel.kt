package com.kvlg.runningtracker.ui.fragments.settings

import android.content.SharedPreferences
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kvlg.runningtracker.domain.CacheManager
import com.kvlg.runningtracker.utils.Constants
import com.kvlg.runningtracker.utils.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for [SettingsFragment]
 *
 * @author Konstantin Koval
 * @since 30.09.2020
 */
class SettingsViewModel @ViewModelInject constructor(
    private val sharedPreferences: SharedPreferences,
    private val cacheManager: CacheManager
) : ViewModel() {

    private val _nameAndEmail = MutableLiveData<Pair<String, String>>()
    private val _weight = MutableLiveData<String>()
    private val _showNameEmailDialog = SingleLiveEvent<Pair<String, String>>()
    private val _showWeightDialog = SingleLiveEvent<String>()
    private val _showSetupScreen = SingleLiveEvent<Unit>()
    private val _showConfirmLogoutDialog = SingleLiveEvent<Unit>()

    /**
     * Name and Email values
     */
    val nameAndEmail: LiveData<Pair<String, String>> = _nameAndEmail

    /**
     * Weight value
     */
    val weight: LiveData<String> = _weight

    /**
     * Event to show alert dialog with name and email input
     */
    val showNameEmailDialog: LiveData<Pair<String, String>> = _showNameEmailDialog

    /**
     * Event to show alert dialog with weight input
     */
    val showWeightDialog: LiveData<String> = _showWeightDialog

    /**
     * Event to show setup screen
     */
    val showSetupScreen: LiveData<Unit> = _showSetupScreen

    /**
     * Show confirm logout dialog
     */
    val confirmLogoutDialog: LiveData<Unit> = _showConfirmLogoutDialog


    /**
     * Load name, email, weight
     */
    fun loadValues() {
        _nameAndEmail.value = getNameEmail()
        _weight.value = getWeight()
    }

    /**
     * Save name and email into [SharedPreferences]
     */
    fun saveNameAndEmail(name: String, email: String) {
        _nameAndEmail.value = Pair(name, email)
        sharedPreferences.edit().apply {
            putString(Constants.KEY_PREF_NAME, name)
            putString(Constants.KEY_PREF_EMAIL, email)
        }.apply()
    }

    /**
     * Save weight into [SharedPreferences]
     */
    fun saveWeight(weight: String) {
        _weight.value = weight
        sharedPreferences.edit().apply {
            putString(Constants.KEY_PREF_WEIGHT, weight)
        }.apply()
    }

    /**
     * Get name and email and show dialog
     */
    fun showNameEmailDialog() {
        _showNameEmailDialog.value = getNameEmail()
    }

    /**
     * Get weight and show dialog
     */
    fun showWeightDialog() {
        _showWeightDialog.value = getWeight()
    }

    /**
     * Clears all the data
     */
    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            sharedPreferences.edit().clear().apply()
            cacheManager.clearApplicationData()
            _showSetupScreen.call()
        }
    }

    fun showConfirmLogoutDialog() {
        _showConfirmLogoutDialog.call()
    }

    private fun getWeight(): String {
        return sharedPreferences.getString(Constants.KEY_PREF_WEIGHT, "70")!!
    }

    private fun getNameEmail(): Pair<String, String> {
        val name = sharedPreferences.getString(Constants.KEY_PREF_NAME, "Name")!!
        val email = sharedPreferences.getString(Constants.KEY_PREF_EMAIL, "Email")!!
        return Pair(name, email)
    }
}