package com.kksionek.gdzietentramwaj.main.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.gms.maps.model.LatLng
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.base.observeNonNullOneEvent
import com.kksionek.gdzietentramwaj.base.viewModel.ViewModelFactory
import com.kksionek.gdzietentramwaj.main.viewModel.MainViewModel
import com.kksionek.gdzietentramwaj.map.view.AdProvider
import com.kksionek.gdzietentramwaj.toLocation
import javax.inject.Inject

private const val LOCATION_PERMISSION_REQUEST = 3456

class MainActivity : AppCompatActivity() {

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory

    @Inject
    internal lateinit var adProvider: AdProvider

    private val mainViewModel: MainViewModel by viewModels(factoryProducer = { viewModelFactory })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupActionBarWithNavController(findNavController(R.id.fragment_maps_content))

        (application as TramApplication).appComponent.inject(this)

        setupLocationPermissionObserver()
        mainViewModel.lastLocation.observeNonNullOneEvent(this) { latLng: LatLng ->
            adProvider.loadAd(latLng.toLocation())
        }

        mainViewModel.appUpdateAvailable.observeNonNullOneEvent(this) { appUpdateAvailability ->
            if (appUpdateAvailability) {
                mainViewModel.startUpdateFlowForResult(this)
            }
        }

        adProvider.showAd(findViewById(R.id.adview_maps_adview))
    }

    @SuppressLint("NewApi")
    private fun setupLocationPermissionObserver() {
        if (mainViewModel.locationPermissionGrantedStatus.value == false) {
            mainViewModel.locationPermissionRequestor.observe(this, Observer {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST
                )
            })
        }
    }

    override fun onSupportNavigateUp(): Boolean =
        findNavController(R.id.fragment_maps_content).navigateUp() || super.onSupportNavigateUp()

    override fun onStart() {
        super.onStart()

        mainViewModel.showGoogleApiUpdateNeededDialog(this) { finish() }
    }

    override fun onResume() {
        super.onResume()
        adProvider.resume()
        mainViewModel.onResume(this)
    }

    override fun onPause() {
        adProvider.pause()
        super.onPause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            mainViewModel.onRequestPermissionsResult(
                permissions.firstOrNull() == Manifest.permission.ACCESS_FINE_LOCATION
                        && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
            )
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
