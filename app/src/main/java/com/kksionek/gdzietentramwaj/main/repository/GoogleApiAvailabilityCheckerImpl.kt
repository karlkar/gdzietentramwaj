package com.kksionek.gdzietentramwaj.main.repository

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

// TODO: Implement AndroidTest? Only Robolectric examples on internet
class GoogleApiAvailabilityCheckerImpl(
    private val context: Context
) : GoogleApiAvailabilityChecker {

    override fun showGoogleApiUpdateNeededDialog(
        activity: Activity,
        requestCode: Int,
        callback: ((DialogInterface) -> Unit)
    ) {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val result = googleApiAvailability.isGooglePlayServicesAvailable(context, 13400000)
        if (result != ConnectionResult.SUCCESS) {
            googleApiAvailability.getErrorDialog(activity, result, requestCode, callback).show()
        }
    }
}