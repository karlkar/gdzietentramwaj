package com.kksionek.gdzietentramwaj

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

fun Fragment.showToast(text: String, long: Boolean = false) {
    Toast.makeText(
        requireContext().applicationContext,
        text,
        if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    ).show()
}

fun Fragment.showToast(@StringRes text: Int, long: Boolean = false) {
    Toast.makeText(
        requireContext().applicationContext,
        text,
        if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    ).show()
}