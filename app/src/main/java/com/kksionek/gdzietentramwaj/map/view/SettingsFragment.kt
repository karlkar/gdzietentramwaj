package com.kksionek.gdzietentramwaj.map.view

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.map.viewModel.MapsViewModel
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : Fragment() {

    private lateinit var viewModel: MapsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        viewModel = ViewModelProviders.of(activity!!)[MapsViewModel::class.java]
    }

    override fun onResume() {
        super.onResume()

        settings_marker_old_radiobutton.isChecked = viewModel.iconSettingsProvider.isOldIconSetEnabled()
        settings_marker_new_radiobutton.isChecked = !viewModel.iconSettingsProvider.isOldIconSetEnabled()

//        settings_marker_old_radiobutton.setOnCheckedChangeListener { _, isChecked ->
//            settings_marker_new_radiobutton.isChecked = false
//            viewModel.iconSettingsManager.setIsOldIconSetEnabled(isChecked)
//        }
//
//        settings_marker_new_radiobutton.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked) {
//                settings_marker_old_radiobutton.isChecked = false
//            }
//        }
    }

    override fun onPause() {
        settings_marker_old_radiobutton.setOnCheckedChangeListener(null)
        settings_marker_new_radiobutton.setOnCheckedChangeListener(null)
        super.onPause()
    }
}