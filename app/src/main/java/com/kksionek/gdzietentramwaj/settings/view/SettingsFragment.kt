package com.kksionek.gdzietentramwaj.settings.view

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.databinding.FragmentSettingsBinding
import com.kksionek.gdzietentramwaj.settings.viewModel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModels({ requireActivity() })

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private var locationChooserFragmentStarted = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentSettingsBinding.inflate(inflater, container, false)
        .also { _binding = it }
        .root

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.settingsMarkerNewBusImageview.apply {
            markerTextview.text = "112"
            root.setOnClickListener { binding.settingsMarkerNewRadiobutton.isChecked = true }
        }
        binding.settingsMarkerNewTramImageview.apply {
            markerTextview.text = "25"
            root.setOnClickListener { binding.settingsMarkerNewRadiobutton.isChecked = true }
        }

        binding.settingsMarkerOldBusImageview.apply {
            markerTextview.text = "112"
            root.setOnClickListener { binding.settingsMarkerOldRadiobutton.isChecked = true }
        }
        binding.settingsMarkerOldTramImageview.apply {
            markerTextview.text = "25"
            root.setOnClickListener { binding.settingsMarkerOldRadiobutton.isChecked = true }
        }

        binding.settingsMarkerOldRadiobutton.apply {
            isChecked = viewModel.oldIconSetEnabled
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    binding.settingsMarkerNewRadiobutton.isChecked = false
                    viewModel.oldIconSetEnabled = true
                }
            }
        }

        binding.settingsMarkerNewRadiobutton.apply {
            isChecked = !viewModel.oldIconSetEnabled
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    binding.settingsMarkerOldRadiobutton.isChecked = false
                    viewModel.oldIconSetEnabled = false
                }
            }
        }

        @Suppress("ConstantConditionIf")
        if (true) { // TODO Consider showing it in free version
            with(binding) {
                settingsRemoveAdsTitle.visibility = View.GONE
                settingsRemoveAdsDescription.visibility = View.GONE
                settingsRemoveAdsButton.visibility = View.GONE
                settingsDividerHorizontal3.visibility = View.GONE
            }
        } else {
            binding.settingsRemoveAdsButton.setOnClickListener {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=com.kksionek.gdzietentramwaj.pro")
                )
                startActivity(intent)
            }
        }

        binding.settingsFavoriteLinesButton.setOnClickListener {
            findNavController().apply {
                if (currentDestination?.id == R.id.destination_settings) {
                    navigate(R.id.destination_favorite)
                }
            }
        }

        with(binding.settingsAutoZoomSwitch) {
            isChecked = viewModel.autoZoomEnabled
            setOnCheckedChangeListener { _, isChecked ->
                viewModel.autoZoomEnabled = isChecked
            }
        }

        binding.settingsStartLocationSwitch.apply {
            isChecked = viewModel.startLocationEnabled
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked && !viewModel.startLocationEnabled) {
                    startChooseLocationFragmentForResult()
                }
                viewModel.startLocationEnabled = isChecked
            }
        }

        binding.settingsBrigadeShowingSwitch.apply {
            isChecked = viewModel.brigadeShowingEnabled
            setOnCheckedChangeListener { _, isChecked ->
                viewModel.brigadeShowingEnabled = isChecked
            }
        }

        binding.settingsTrafficShowingSwitch.apply {
            isChecked = viewModel.trafficShowingEnabled
            setOnCheckedChangeListener { _, isChecked ->
                viewModel.trafficShowingEnabled = isChecked
            }
        }

        val sortedCities = Cities.values().sortedBy { it.name }
        binding.settingsCitySpinner.apply {
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
                    binding.settingsCityDescription.text = getString(
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
                binding.settingsStartLocationSwitch.isChecked = false
            }
            locationChooserFragmentStarted = false
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    private fun startChooseLocationFragmentForResult() {
        locationChooserFragmentStarted = true
        findNavController().apply {
            if (currentDestination?.id == R.id.destination_settings) {
                navigate(R.id.destination_chooseStartLocation)
            }
        }
    }
}