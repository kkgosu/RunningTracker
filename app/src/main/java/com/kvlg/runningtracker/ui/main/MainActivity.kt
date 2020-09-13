package com.kvlg.runningtracker.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.kvlg.runningtracker.R
import com.kvlg.runningtracker.databinding.ActivityMainBinding
import com.kvlg.runningtracker.utils.BnvVisibilityListener
import com.kvlg.runningtracker.utils.Constants
import com.kvlg.runningtracker.utils.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), BnvVisibilityListener {
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    private var currentNavController: LiveData<NavController>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navGraphIds = intArrayOf(
            R.navigation.runs,
            R.navigation.statistics,
            R.navigation.profile
        )

        val controller = binding.bnv.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_fragment,
            intent = intent
        )

        currentNavController = controller
        navigateToTrackingFragmentIfNeeded(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: false
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    override fun hide(hide: Boolean) {
        binding.bnv.visibility = if (hide) View.GONE else View.VISIBLE
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        if (intent?.action == Constants.ACTION_SHOW_TRACKING_FRAGMENT) {
            currentNavController?.value?.navigate(R.id.action_global_trackingFragment)
        }
    }

}