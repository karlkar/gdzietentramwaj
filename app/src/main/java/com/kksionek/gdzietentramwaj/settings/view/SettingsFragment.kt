package com.kksionek.gdzietentramwaj.settings.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.kksionek.gdzietentramwaj.BuildConfig
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.base.createDialogView
import com.kksionek.gdzietentramwaj.base.viewModel.ViewModelFactory
import com.kksionek.gdzietentramwaj.favorite.view.FavoriteLinesActivity
import com.kksionek.gdzietentramwaj.settings.viewModel.SettingsViewModel
import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject

class SettingsFragment : Fragment(), OnBackPressedCallback {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onAttach(context: Context) {
        super.onAttach(context)

        (activity!!.application as TramApplication).appComponent.inject(this)
        viewModel =
            ViewModelProviders.of(activity!!, viewModelFactory)[SettingsViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settings_marker_new_bus_imageview.apply {
            findViewById<TextView>(R.id.marker_textview).text = "112"
            setOnClickListener { settings_marker_new_radiobutton.isChecked = true }
        }
        settings_marker_new_tram_imageview.apply {
            findViewById<TextView>(R.id.marker_textview).text = "25"
            setOnClickListener { settings_marker_new_radiobutton.isChecked = true }
        }

        settings_marker_old_bus_imageview.apply {
            findViewById<TextView>(R.id.marker_textview).text = "112"
            setOnClickListener { settings_marker_old_radiobutton.isChecked = true }
        }
        settings_marker_old_tram_imageview.apply {
            findViewById<TextView>(R.id.marker_textview).text = "25"
            setOnClickListener { settings_marker_old_radiobutton.isChecked = true }
        }

        settings_marker_old_radiobutton.apply {
            isChecked = viewModel.isOldIconSetEnabled()
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    settings_marker_new_radiobutton.isChecked = false
                    viewModel.setIsOldIconSetEnabled(true)
                }
            }
        }

        settings_marker_new_radiobutton.apply {
            isChecked = !viewModel.isOldIconSetEnabled()
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    settings_marker_old_radiobutton.isChecked = false
                    viewModel.setIsOldIconSetEnabled(false)
                }
            }
        }

        @Suppress("ConstantConditionIf")
        if (BuildConfig.FLAVOR == "paid") {
            settings_remove_ads_button.visibility = View.GONE
            settings_divider_horizontal_3.visibility = View.GONE
        } else {
            settings_remove_ads_button.setOnClickListener {
                val dialogView =
                    createDialogView(context!!.applicationContext, R.string.remove_info)
                        ?: return@setOnClickListener
                AlertDialog.Builder(activity!!)
                    .setTitle(R.string.remove_title)
                    .setView(dialogView)
                    .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        }

        settings_favorite_lines_button.setOnClickListener {
            val intent = Intent(
                context!!.applicationContext,
                FavoriteLinesActivity::class.java
            ) // TODO Change it to fragment
            startActivity(intent)
        }
    }

    override fun handleOnBackPressed(): Boolean = findNavController().navigateUp()
}