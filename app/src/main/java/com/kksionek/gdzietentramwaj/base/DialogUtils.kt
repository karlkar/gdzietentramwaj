package com.kksionek.gdzietentramwaj.base

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import com.kksionek.gdzietentramwaj.R

@SuppressLint("InflateParams")
@Suppress("DEPRECATION")
fun createDialogView(context: Context, @StringRes textId: Int): View? {
    val view = LayoutInflater.from(context)
        .inflate(R.layout.dialog_info, null)
    view.findViewById<TextView>(R.id.info_dialog_text)?.apply {
        movementMethod = LinkMovementMethod.getInstance()
        text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(context.getString(textId), Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(context.getString(textId))
        }
    }
    return view
}