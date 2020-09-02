package com.kvlg.runningtracker.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.kvlg.runningtracker.R
import com.kvlg.runningtracker.adapters.RunAdapter
import com.kvlg.runningtracker.adapters.RunDiffCallback
import com.kvlg.runningtracker.databinding.FragmentRunBinding
import com.kvlg.runningtracker.ui.viewmodels.MainViewModel
import com.kvlg.runningtracker.ui.viewmodels.RunsLiveDataRegistry
import com.kvlg.runningtracker.utils.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.kvlg.runningtracker.utils.SortTypes
import com.kvlg.runningtracker.utils.TrackingUtils
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject

/**
 * [Fragment] with list of all runs
 *
 * @author Konstantin Koval
 * @since 19.07.2020
 */
@AndroidEntryPoint
class RunFragment : Fragment(), EasyPermissions.PermissionCallbacks {
    private val viewModel: MainViewModel by viewModels()

    private var _binding: FragmentRunBinding? = null
    private val binding get() = _binding!!

    private var runsAdapter: RunAdapter? = null

    @Inject
    lateinit var runsLiveDataRegistry: RunsLiveDataRegistry

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRunBinding.inflate(inflater, container, false)
        runsAdapter = RunAdapter(RunDiffCallback(), viewLifecycleOwner, runsLiveDataRegistry)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requestPermissions()
        binding.runsRecyclerView.adapter = runsAdapter
        binding.runsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    binding.addFab.shrink()
                } else {
                    binding.addFab.extend()
                }
            }
        })

        val spinnerAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, resources.getStringArray(R.array.filter_options))
        binding.filterSpinner.adapter = spinnerAdapter
        binding.filterSpinner.setSelection(viewModel.sortType.ordinal)
        binding.filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.sortRuns(SortTypes.values()[position])
            }
        }

        viewModel.runs.observe(viewLifecycleOwner) {
            runsAdapter?.submitList(it)
            binding.runsRecyclerView.smoothScrollToPosition(0)
        }
        binding.addFab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        0 -> {
            viewModel.deleteRun(item.groupId)
            true
        }
        else -> super.onContextItemSelected(item)
    }

    private fun requestPermissions() {
        if (TrackingUtils.hasLocationPermissions(requireContext())) {
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "This permission is needed to track your position during activity.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "This permission is needed to track your position during activity.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }
}