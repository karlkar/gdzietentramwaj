package com.kksionek.gdzietentramwaj.map.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.lifecycle.LifecycleOwner
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.base.observeNonNull
import com.kksionek.gdzietentramwaj.base.view.ImageLoader
import com.kksionek.gdzietentramwaj.makeExhaustive
import com.kksionek.gdzietentramwaj.map.model.DifficultiesState
import com.kksionek.gdzietentramwaj.map.viewModel.MapsViewModel
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.bottom_sheet_difficulties.*

class DifficultiesBottomSheet(
    override val containerView: View,
    context: Context,
    lifecycleOwner: LifecycleOwner,
    viewModel: MapsViewModel,
    imageLoader: ImageLoader
) : LayoutContainer {

    private val difficultiesObserver: (UiState<DifficultiesState>) -> Unit = { uiState ->
        when (uiState) {
            is UiState.Success -> {
                if (uiState.data.isSupported) {
                    containerView.visibility = View.VISIBLE
                    stopDifficultiesLoading()
                    textview_difficulties_title.text =
                        context.getString(
                            R.string.difficulties_bottom_sheet_title,
                            uiState.data.difficultiesEntities.size
                        )
                    if (uiState.data.difficultiesEntities.isEmpty()) {
                        textview_difficulties_message.text =
                            context.getText(R.string.difficulties_bottom_sheet_no_items)
                        recyclerview_difficulties_difficulties.visibility = View.GONE
                        textview_difficulties_message.visibility = View.VISIBLE
                    } else {
                        textview_difficulties_message.visibility = View.GONE
                        recyclerview_difficulties_difficulties.visibility = View.VISIBLE
                        (recyclerview_difficulties_difficulties.adapter as DifficultiesAdapter).submitList(
                            uiState.data.difficultiesEntities
                        )
                    }
                } else {
                    containerView.visibility = View.GONE
                }
            }
            is UiState.Error -> {
                containerView.visibility = View.VISIBLE
                textview_difficulties_title.text =
                    context.getString(R.string.difficulties_bottom_sheet_title, 0)
                textview_difficulties_message.text =
                    context.getString(uiState.message, uiState.args)
                recyclerview_difficulties_difficulties.visibility = View.GONE
                textview_difficulties_message.visibility = View.VISIBLE
                stopDifficultiesLoading()
            }
            is UiState.InProgress -> {
                containerView.visibility = View.VISIBLE
                startDifficultiesLoading()
            }
        }.makeExhaustive
    }

    init {
        recyclerview_difficulties_difficulties.adapter = DifficultiesAdapter(imageLoader) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.link))
            val chooser = Intent.createChooser(
                intent,
                context.getString(R.string.difficulties_bottom_sheet_link_title)
            )
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(chooser)
            }
        }

        imagebutton_difficulties_refresh_button.setOnClickListener {
            viewModel.forceReloadDifficulties()
        }

        viewModel.difficulties.observeNonNull(lifecycleOwner, difficultiesObserver)
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