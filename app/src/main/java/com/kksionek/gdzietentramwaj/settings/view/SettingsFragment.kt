package com.kksionek.gdzietentramwaj.settings.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.kksionek.gdzietentramwaj.BuildConfig
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.base.dataSource.Cities
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

    @SuppressLint("SetTextI18n")
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
            settings_remove_ads_title.visibility = View.GONE
            settings_remove_ads_description.visibility = View.GONE
            settings_remove_ads_button.visibility = View.GONE
            settings_divider_horizontal_3.visibility = View.GONE
        } else {
            settings_remove_ads_button.setOnClickListener {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=com.kksionek.gdzietentramwaj.pro")
                )
                startActivity(intent)
            }
        }

        settings_favorite_lines_button.setOnClickListener {
            findNavController().navigate(R.id.destination_favorite)
        }

        settings_auto_zoom_switch.isChecked = viewModel.autoZoomEnabled

        settings_auto_zoom_switch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.autoZoomEnabled = isChecked
        }

        settings_start_location_switch.apply {
            isChecked = viewModel.startLocationEnabled
            setOnCheckedChangeListener { _, isChecked ->

                if (isChecked && !viewModel.startLocationEnabled) {
                    startChooseLocationFragmentForResult()
                }
                viewModel.startLocationEnabled = isChecked
            }
        }

        settings_brigade_showing_switch.apply {
            isChecked = viewModel.brigadeShowingEnabled
            setOnCheckedChangeListener { _, isChecked ->
                viewModel.brigadeShowingEnabled = isChecked
            }
        }

        settings_traffic_showing_switch.apply {
            isChecked = viewModel.trafficShowingEnabled
            setOnCheckedChangeListener { _, isChecked ->
                viewModel.trafficShowingEnabled = isChecked
            }
        }

        val sortedCities = Cities.values().sortedBy { it.name }
        settings_city_spinner.apply {
            adapter = ArrayAdapter(
                view.context,
                android.R.layout.simple_spinner_dropdown_item,
                sortedCities.map { getString(it.humanReadableName) }
            )
            setSelection(sortedCities.indexOf(viewModel.city))
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedCity = sortedCities[position]
                    viewModel.city = selectedCity
                    settings_city_description.text = getString(
                        when (selectedCity) {
                            Cities.WARSAW -> R.string.city_warsaw_description
                            Cities.KRAKOW -> R.string.city_krakow_description
                            Cities.WROCLAW -> R.string.city_wroclaw_description
                            Cities.LODZ -> R.string.city_lodz_description
                            Cities.SZCZECIN -> R.string.city_szczecin_description
                            Cities.BIELSKO -> R.string.city_bielsko_description
                            Cities.ZIELONA -> R.string.city_zielona_description
                            Cities.GOP -> R.string.city_gop_description
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (locationChooserFragmentStarted) {
            if (!viewModel.locationChooserFragmentClosedWithResult) {
                settings_start_location_switch.isChecked = false
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