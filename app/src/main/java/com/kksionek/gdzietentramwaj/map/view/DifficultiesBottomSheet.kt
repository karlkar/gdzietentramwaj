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
import com.kksionek.gdzietentramwaj.databinding.BottomSheetDifficultiesBinding
import com.kksionek.gdzietentramwaj.makeExhaustive
import com.kksionek.gdzietentramwaj.map.model.DifficultiesState
import com.kksionek.gdzietentramwaj.map.viewModel.MapsViewModel

class DifficultiesBottomSheet(
    private val binding: BottomSheetDifficultiesBinding,
    context: Context,
    lifecycleOwner: LifecycleOwner,
    viewModel: MapsViewModel,
    imageLoader: ImageLoader
) {

    private val difficultiesObserver: (UiState<DifficultiesState>) -> Unit = { uiState ->
        when (uiState) {
            is UiState.Success -> {
                if (uiState.data.isSupported) {
                    binding.root.visibility = View.VISIBLE
                    stopDifficultiesLoading()
                    binding.textviewDifficultiesTitle.text =
                        context.getString(
                            R.string.difficulties_bottom_sheet_title,
                            uiState.data.difficultiesEntities.size
                        )
                    if (uiState.data.difficultiesEntities.isEmpty()) {
                        binding.textviewDifficultiesMessage.text =
                            context.getText(R.string.difficulties_bottom_sheet_no_items)
                        binding.recyclerviewDifficultiesDifficulties.visibility = View.GONE
                        binding.textviewDifficultiesMessage.visibility = View.VISIBLE
                    } else {
                        binding.textviewDifficultiesMessage.visibility = View.GONE
                        binding.recyclerviewDifficultiesDifficulties.visibility = View.VISIBLE
                        (binding.recyclerviewDifficultiesDifficulties.adapter as DifficultiesAdapter).submitList(
                            uiState.data.difficultiesEntities
                        )
                    }
                } else {
                    binding.root.visibility = View.GONE
                }
            }
            is UiState.Error -> {
                binding.root.visibility = View.VISIBLE
                binding.textviewDifficultiesTitle.text =
                    context.getString(R.string.difficulties_bottom_sheet_title, 0)
                binding.textviewDifficultiesMessage.text =
                    context.getString(uiState.message, uiState.args)
                binding.recyclerviewDifficultiesDifficulties.visibility = View.GONE
                binding.textviewDifficultiesMessage.visibility = View.VISIBLE
                stopDifficultiesLoading()
            }
            is UiState.InProgress -> {
                binding.root.visibility = View.VISIBLE
                startDifficultiesLoading()
            }
        }.makeExhaustive
    }

    init {
        binding.recyclerviewDifficultiesDifficulties.adapter = DifficultiesAdapter(imageLoader) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.link))
            val chooser = Intent.createChooser(
                intent,
                context.getString(R.string.difficulties_bottom_sheet_link_title)
            )
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(chooser)
            }
        }

        binding.imagebuttonDifficultiesRefreshButton.setOnClickListener {
            viewModel.forceReloadDifficulties()
        }

        viewModel.difficulties.observeNonNull(lifecycleOwner, difficultiesObserver)
    }

    private val reloadAnimation: Animation by lazy {
        AnimationUtils.loadAnimation(context, R.anim.anim_rotate)
    }

    private fun startDifficultiesLoading() {
        with(binding.imagebuttonDifficultiesRefreshButton) {
            isEnabled = false
            startAnimation(reloadAnimation)
        }
    }

    private fun stopDifficultiesLoading() {
        with(binding.imagebuttonDifficultiesRefreshButton) {
            isEnabled = true
            clearAnimation()
        }
    }
}