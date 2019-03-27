package com.kksionek.gdzietentramwaj

import android.widget.Toast
import androidx.fragment.app.Fragment


fun Fragment.showSuccessToast(text: String) {
    context?.let {
        Toast.makeText(
            it.applicationContext,
            text,
            Toast.LENGTH_SHORT
        ).show()
    }
}

fun Fragment.showErrorToast(text: String) {
    context?.let {
        Toast.makeText(
            it.applicationContext,
            text,
            Toast.LENGTH_LONG
        ).show()
    }
}