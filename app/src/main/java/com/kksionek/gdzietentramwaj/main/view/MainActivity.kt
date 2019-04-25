package com.kksionek.gdzietentramwaj.main.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.base.viewModel.ViewModelFactory
import com.kksionek.gdzietentramwaj.main.viewModel.MainViewModel
import com.kksionek.gdzietentramwaj.map.view.AdProviderInterface
import javax.inject.Inject

private const val MY_GOOGLE_API_AVAILABILITY_REQUEST = 2345
private const val LOCATION_PERMISSION_REQUEST = 3456

interface LocationChangeListener {
    fun onLocationChanged(location: Location)
}

class MainActivity : AppCompatActivity(), LocationChangeListener {

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory

    @Inject
    internal lateinit var adProviderInterface: AdProviderInterface

    private lateinit var viewModel: MainViewModel

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val appBarConfiguration = AppBarConfiguration(setOf(R.id.destination_onboarding, R.id.destination_map))
        setupActionBarWithNavController(
            findNavController(R.id.fragment_maps_content),
            appBarConfiguration
        )

        (application as TramApplication).appComponent.inject(this)

        viewModel = ViewModelProviders.of(this, viewModelFactory)[MainViewModel::class.java]

        if (viewModel.locationPermission.value == false) {
            viewModel.requestLocationPermission.observe(this, Observer {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST
                )
            })
        }

        adProviderInterface.initialize(this, getString(R.string.adMobAppId))
        adProviderInterface.showAd(findViewById(R.id.adview_maps_adview))
    }

    override fun onSupportNavigateUp(): Boolean =
        findNavController(R.id.fragment_maps_content).navigateUp() || super.onSupportNavigateUp()

    override fun onStart() {
        super.onStart()

        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val result = googleApiAvailability.isGooglePlayServicesAvailable(this, 13400000)
        if (result != ConnectionResult.SUCCESS) {
            googleApiAvailability.getErrorDialog(
                this, result,
                MY_GOOGLE_API_AVAILABILITY_REQUEST
            ) {
                finish()
            }.show()
        }
    }

    override fun onResume() {
        super.onResume()
        adProviderInterface.resume()
    }

    override fun onPause() {
        adProviderInterface.pause()
        super.onPause()
    }

    override fun onLocationChanged(location: Location) {
        adProviderInterface.loadAd(applicationContext, location)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        viewModel.updateLocationPermission(
            requestCode == LOCATION_PERMISSION_REQUEST
                    && permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
        )
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
