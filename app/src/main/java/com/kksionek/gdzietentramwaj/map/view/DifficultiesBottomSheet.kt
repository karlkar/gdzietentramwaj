package com.kksionek.gdzietentramwaj.map.view

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.makeExhaustive
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesEntity
import com.kksionek.gdzietentramwaj.map.viewModel.MapsViewModel
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.bottom_sheet_difficulties.*

class DifficultiesBottomSheet(
    override val containerView: View,
    context: Context,
    lifecycleOwner: LifecycleOwner,
    viewModel: MapsViewModel
) : LayoutContainer {

    private val difficultiesObserver =
        Observer { difficulties: UiState<List<DifficultiesEntity>>? ->
            when (difficulties) {
                is UiState.Success -> {
                    stopDifficultiesLoading()
                    if (difficulties.data.isEmpty()) {
                        textview_difficulties_no_items.visibility = View.VISIBLE
                    } else {
                        textview_difficulties_no_items.visibility = View.GONE
                        (recyclerview_difficulties_difficulties.adapter as DifficultiesAdapter).submitList(
                            difficulties.data
                        )
                    }
                }
                is UiState.Error -> {
                    stopDifficultiesLoading()
                }
                is UiState.InProgress -> {
                    startDifficultiesLoading()
                }
                null -> {
                }
            }.makeExhaustive
        }

    init {
        recyclerview_difficulties_difficulties.adapter = DifficultiesAdapter {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.link)))
        }

        imagebutton_difficulties_refresh_button.setOnClickListener {
            viewModel.forceReloadDifficulties()
        }

        viewModel.difficulties.observe(lifecycleOwner, difficultiesObserver)
    }

    private val reloadAnimation: Animation by lazy {
        AnimationUtils.loadAnimation(context, R.anim.anim_rotate)
    }

    private fun startDifficultiesLoading() {
        imagebutton_difficulties_refresh_button.isEnabled = false
        imagebutton_difficulties_refresh_button.startAnimation(reloadAnimation)
    }

    private fun stopDifficultiesLoading() {
        imagebutton_difficulties_refresh_button.isEnabled = true
        imagebutton_difficulties_refresh_button.clearAnimation()
    }
}