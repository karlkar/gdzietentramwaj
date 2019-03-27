package com.kksionek.gdzietentramwaj.settings.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.base.viewModel.ViewModelFactory
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
        viewModel = ViewModelProviders.of(activity!!, viewModelFactory)[SettingsViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settings_marker_old_radiobutton.isChecked = viewModel.isOldIconSetEnabled()
        settings_marker_new_radiobutton.isChecked = !viewModel.isOldIconSetEnabled()

        settings_marker_old_radiobutton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                settings_marker_new_radiobutton.isChecked = false
                viewModel.setIsOldIconSetEnabled(true)
            }
        }

        settings_marker_new_radiobutton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                settings_marker_old_radiobutton.isChecked = false
                viewModel.setIsOldIconSetEnabled(false)
            }
        }
    }

    override fun handleOnBackPressed(): Boolean {
        findNavController().navigateUp()
        return true
    }
}