package com.kksionek.gdzietentramwaj.settings.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.kksionek.gdzietentramwaj.BuildConfig
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.base.createDialogView
import com.kksionek.gdzietentramwaj.base.viewModel.ViewModelFactory
import com.kksionek.gdzietentramwaj.settings.viewModel.SettingsViewModel
import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject

class SettingsFragment : Fragment(), OnBackPressedCallback {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: SettingsViewModel

    private var locationChooserFragmentStarted = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onAttach(context: Context) {
        super.onAttach(context)

        (context.applicationContext as TramApplication).appComponent.inject(this)
        viewModel =
            ViewModelProviders.of(activity!!, viewModelFactory)[SettingsViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settings_marker_new_bus_imageview.apply {
            findViewById<TextView>(R.id.marker_textview).text = "112"
            setOnClickListener { settings_marker_new_radiobutton.isChecked = true }
        }
        settings_marker_new_tram_imageview.apply {
            findViewById<TextView>(R.id.marker_textview).text = "25"
            setOnClickListener { settings_marker_new_radiobutton.isChecked = true }
        }

        settings_marker_old_bus_imageview.apply {
            findViewById<TextView>(R.id.marker_textview).text = "112"
            setOnClickListener { settings_marker_old_radiobutton.isChecked = true }
        }
        settings_marker_old_tram_imageview.apply {
            findViewById<TextView>(R.id.marker_textview).text = "25"
            setOnClickListener { settings_marker_old_radiobutton.isChecked = true }
        }

        settings_marker_old_radiobutton.apply {
            isChecked = viewModel.oldIconSetEnabled
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    settings_marker_new_radiobutton.isChecked = false
                    viewModel.oldIconSetEnabled = true
                }
            }
        }

        settings_marker_new_radiobutton.apply {
            isChecked = !viewModel.oldIconSetEnabled
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    settings_marker_old_radiobutton.isChecked = false
                    viewModel.oldIconSetEnabled = false
                }
            }
        }

        @Suppress("ConstantConditionIf")
        if (BuildConfig.FLAVOR == "paid") {
            settings_remove_ads_button.visibility = View.GONE
            settings_divider_horizontal_3.visibility = View.GONE
        } else {
            settings_remove_ads_button.setOnClickListener {
                val dialogView =
                    createDialogView(view.context, R.string.remove_info)
                        ?: return@setOnClickListener
                activity?.let {
                    AlertDialog.Builder(it)
                        .setTitle(R.string.remove_title)
                        .setView(dialogView)
                        .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                        .show()
                }
            }
        }

        settings_favorite_lines_button.setOnClickListener {
            findNavController().navigate(R.id.destination_favorite)
        }

        settings_auto_zoom_checkbox.isChecked = viewModel.autoZoomEnabled

        settings_auto_zoom_checkbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.autoZoomEnabled = isChecked
        }

        settings_start_location_checkbox.apply {
            isChecked = viewModel.startLocationEnabled
            setOnCheckedChangeListener { _, isChecked ->

                if (isChecked && !viewModel.startLocationEnabled) {
                    startChooseLocationFragmentForResult()
                }
                viewModel.startLocationEnabled = isChecked
            }
        }

        settings_brigade_showing_checkbox.apply {
            isChecked = viewModel.brigadeShowingEnabled
            setOnCheckedChangeListener { _, isChecked ->
                viewModel.brigadeShowingEnabled = isChecked
            }
        }

        settings_traffic_showing_checkbox.apply {
            isChecked = viewModel.trafficShowingEnabled
            setOnCheckedChangeListener { _, isChecked ->
                viewModel.trafficShowingEnabled = isChecked
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (locationChooserFragmentStarted) {
            if (!viewModel.locationChooserFragmentClosedWithResult) {
                settings_start_location_checkbox.isChecked = false
            }
            locationChooserFragmentStarted = false
        }
    }

    private fun startChooseLocationFragmentForResult() {
        locationChooserFragmentStarted = true
        findNavController().navigate(R.id.destination_chooseStartLocation)
    }

    override fun handleOnBackPressed(): Boolean = findNavController().navigateUp()
}