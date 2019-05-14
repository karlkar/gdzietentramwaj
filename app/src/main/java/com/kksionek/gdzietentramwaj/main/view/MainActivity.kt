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

class MainActivity : AppCompatActivity() {

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory

    @Inject
    internal lateinit var adProviderInterface: AdProviderInterface

    private lateinit var mainViewModel: MainViewModel

    private val lastLocationObserver = Observer { location: Location? ->
        location?.let {
            adProviderInterface.loadAd(applicationContext, it)
        }
    }

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupActionBarWithNavController(findNavController(R.id.fragment_maps_content))

        (application as TramApplication).appComponent.inject(this)

        mainViewModel = ViewModelProviders.of(this, viewModelFactory)[MainViewModel::class.java]

        if (mainViewModel.locationPermission.value == false) {
            mainViewModel.locationPermissionRequestLiveData.observe(this, Observer {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST
                )
            })
        }
        mainViewModel.lastLocation.observe(this, lastLocationObserver)

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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mainViewModel.updateLocationPermission(
            requestCode == LOCATION_PERMISSION_REQUEST
                    && permissions.isNotEmpty()
                    && grantResults.isNotEmpty()
                    && permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
        )
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
