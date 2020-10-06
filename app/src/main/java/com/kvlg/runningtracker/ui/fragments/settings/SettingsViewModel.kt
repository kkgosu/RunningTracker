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

    val nameAndEmail: LiveData<Pair<String, String>> = _nameAndEmail
    val weight: LiveData<String> = _weight
    val showNameEmailDialog: LiveData<Pair<String, String>> = _showNameEmailDialog
    val showWeightDialog: LiveData<String> = _showWeightDialog
    val showSetupScreen: LiveData<Unit> = _showSetupScreen

    fun loadValues() {
        _nameAndEmail.value = getNameEmail()
        _weight.value = getWeight()
    }

    fun saveNameAndEmail(name: String, email: String) {
        _nameAndEmail.value = Pair(name, email)
        sharedPreferences.edit().apply {
            putString(Constants.KEY_PREF_NAME, name)
            putString(Constants.KEY_PREF_EMAIL, email)
        }.apply()
    }

    fun saveWeight(weight: String) {
        _weight.value = weight
        sharedPreferences.edit().apply {
            putString(Constants.KEY_PREF_WEIGHT, weight)
        }.apply()
    }

    fun showNameEmailDialog() {
        _showNameEmailDialog.value = getNameEmail()
    }

    fun showWeightDialog() {
        _showWeightDialog.value = getWeight()
    }

    fun onLogoutClick() {
        viewModelScope.launch(Dispatchers.IO) {
            sharedPreferences.edit().clear().apply()
            cacheManager.clearApplicationData()
            _showSetupScreen.call()
        }
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