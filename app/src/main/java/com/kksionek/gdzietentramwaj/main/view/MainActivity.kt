package com.kksionek.gdzietentramwaj.main.view

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.base.createDialogView
import com.kksionek.gdzietentramwaj.base.viewModel.ViewModelFactory
import com.kksionek.gdzietentramwaj.main.viewModel.MainViewModel
import javax.inject.Inject

private const val MY_GOOGLE_API_AVAILABILITY_REQUEST = 2345

interface AboutDialogProvider {
    fun showAboutAppDialog()
}

class MainActivity : AppCompatActivity(),
    AboutDialogProvider {

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        setupActionBarWithNavController(findNavController(R.id.fragment_maps_content))

        (application as TramApplication).appComponent.inject(this)

        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(MainViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()

        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val result = googleApiAvailability.isGooglePlayServicesAvailable(this)
        if (result != ConnectionResult.SUCCESS) {
            googleApiAvailability.getErrorDialog(this, result,
                MY_GOOGLE_API_AVAILABILITY_REQUEST
            ) {
                finish()
            }.show()
        }

        if (viewModel.shouldShowWelcomeDialog()) {
            showAboutAppDialog()
        }
    }

    override fun showAboutAppDialog() {
        val view = createDialogView(this, R.string.disclaimer) ?: return
        AlertDialog.Builder(this)
            .setTitle(R.string.about_app)
            .setView(view)
            .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}