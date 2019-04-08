package com.kksionek.gdzietentramwaj.main.view

import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.kksionek.gdzietentramwaj.BuildConfig
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.base.createDialogView
import com.kksionek.gdzietentramwaj.base.viewModel.ViewModelFactory
import com.kksionek.gdzietentramwaj.main.viewModel.MainViewModel
import com.kksionek.gdzietentramwaj.map.view.AdProviderInterface
import javax.inject.Inject

private const val MY_GOOGLE_API_AVAILABILITY_REQUEST = 2345

interface AboutDialogProvider {
    fun showAboutAppDialog()
}

interface LocationChangeListener {
    fun onLocationChanged(location: Location)
}

class MainActivity : AppCompatActivity(), AboutDialogProvider, LocationChangeListener {

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory

    @Inject
    internal lateinit var adProviderInterface: AdProviderInterface

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupActionBarWithNavController(findNavController(R.id.fragment_maps_content))

        (application as TramApplication).appComponent.inject(this)

        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(MainViewModel::class.java)

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

        if (viewModel.shouldShowWelcomeDialog()) {
            showAboutAppDialog()
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

    override fun showAboutAppDialog() {
        val view = createDialogView(
            this,
            getString(R.string.disclaimer, BuildConfig.APPLICATION_ID)
        ) ?: return
        AlertDialog.Builder(this)
            .setTitle(R.string.about_app)
            .setView(view)
            .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onLocationChanged(location: Location) {
        adProviderInterface.loadAd(applicationContext, location)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
