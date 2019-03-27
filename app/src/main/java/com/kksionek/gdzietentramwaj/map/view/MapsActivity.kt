package com.kksionek.gdzietentramwaj.map.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.base.viewModel.ViewModelFactory
import com.kksionek.gdzietentramwaj.map.viewModel.MapsViewModel
import javax.inject.Inject

private const val MY_GOOGLE_API_AVAILABILITY_REQUEST = 2345

class MapsActivity : AppCompatActivity() {
    // TODO Move to single activity

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: MapsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        (application as TramApplication).appComponent.inject(this)

        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(MapsViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val result = googleApiAvailability.isGooglePlayServicesAvailable(this)
        if (result != ConnectionResult.SUCCESS) {
            googleApiAvailability.getErrorDialog(this, result, MY_GOOGLE_API_AVAILABILITY_REQUEST) {
                finish()
            }.show()
        }
    }

    companion object {
        private const val TAG = "MapsActivity"
    }
}
