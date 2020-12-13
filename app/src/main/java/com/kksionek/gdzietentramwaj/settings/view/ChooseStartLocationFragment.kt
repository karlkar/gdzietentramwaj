package com.kksionek.gdzietentramwaj.settings.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.WARSAW_LATLNG
import com.kksionek.gdzietentramwaj.settings.viewModel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_settings_start_location.*

@AndroidEntryPoint
class ChooseStartLocationFragment : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap

    private val viewModel: SettingsViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_settings_start_location, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.locationChooserFragmentClosedWithResult = false

        if (!this::map.isInitialized) {
            (childFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment)
                    ?.getMapAsync(this)
        }

        settings_start_location_choose_button.setOnClickListener {
            if (this::map.isInitialized) {
                map.cameraPosition.let {
                    viewModel.saveStartLocation(it.target, it.zoom)
                    viewModel.locationChooserFragmentClosedWithResult = true
                    findNavController().apply {
                        if (currentDestination?.id == R.id.destination_chooseStartLocation) {
                            navigateUp()
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val startLocationPosition = viewModel.startLocationPosition ?: WARSAW_LATLNG
        val startPositionZoom = viewModel.startLocationZoom ?: 15f
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocationPosition, startPositionZoom))
        map.uiSettings.isZoomControlsEnabled = true
        map.isMyLocationEnabled = viewModel.locationPermissionGranted
    }
}