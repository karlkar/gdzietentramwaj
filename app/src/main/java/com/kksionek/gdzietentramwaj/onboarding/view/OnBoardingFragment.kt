package com.kksionek.gdzietentramwaj.onboarding.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.base.viewModel.ViewModelFactory
import com.kksionek.gdzietentramwaj.main.viewModel.MainViewModel
import com.kksionek.gdzietentramwaj.onboarding.viewmodel.OnBoardingViewModel
import kotlinx.android.synthetic.main.fragment_onboarding.*
import javax.inject.Inject

class OnBoardingFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var onBoardingViewModel: OnBoardingViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val sortedCities = Cities.values().sortedBy { it.name }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_onboarding, container, false)

    override fun onAttach(context: Context) {
        super.onAttach(context)

        (context.applicationContext as TramApplication).appComponent.inject(this)
        mainViewModel =
            ViewModelProviders.of(activity!!, viewModelFactory)[MainViewModel::class.java]
        onBoardingViewModel =
            ViewModelProviders.of(this, viewModelFactory)[OnBoardingViewModel::class.java]

        if (onBoardingViewModel.skipOnBoarding) {
            findNavController().navigate(OnBoardingFragmentDirections.actionOnBoardingFragmentToDestinationMap())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (mainViewModel.locationPermission.value == false) {
            mainViewModel.locationPermission.observe(this, Observer { permissionGranted ->
                if (permissionGranted) {
                    onboarding_allow_location_button.isEnabled = false
                    onBoardingViewModel.forceReloadLocation()
                }
            })
        }

        onBoardingViewModel.nearestCity.observe(this, Observer { city: Cities? ->
            onboarding_city_spinner.setSelection(sortedCities.indexOf(city))
        })

        val locationPermissionGranted = mainViewModel.locationPermission.value ?: false
        onboarding_allow_location_button.apply {
            isEnabled = !locationPermissionGranted
            setOnClickListener {
                mainViewModel.requestLocationPermission()
            }
        }

        onboarding_city_spinner.apply {
            adapter = ArrayAdapter(
                view.context,
                android.R.layout.simple_spinner_dropdown_item,
                sortedCities.map { getString(it.humanReadableName) }
            )
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
                    onBoardingViewModel.city = selectedCity
                    onboarding_city_description_textview.text = getString(
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
            setSelection(sortedCities.indexOf(onBoardingViewModel.city), false)
        }

        onboarding_start_button.setOnClickListener {
            findNavController().apply {
                if (currentDestination?.id == R.id.destination_onboarding) {
                    navigate(OnBoardingFragmentDirections.actionOnBoardingFragmentToDestinationMap())
                }
            }
            onBoardingViewModel.updateLastVersion()
        }

        if (locationPermissionGranted) {
            onBoardingViewModel.forceReloadLocation()
        }
    }
}