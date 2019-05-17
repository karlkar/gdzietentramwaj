package com.kksionek.gdzietentramwaj.main.repository

import android.app.Activity
import android.content.DialogInterface

interface GoogleApiAvailabilityChecker {

    fun showGoogleApiUpdateNeededDialog(
        activity: Activity,
        requestCode: Int,
        callback: ((DialogInterface) -> Unit)
    )
}