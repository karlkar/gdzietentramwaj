package com.kksionek.gdzietentramwaj.favorite.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.base.observeNonNull
import com.kksionek.gdzietentramwaj.favorite.viewModel.FavoriteLinesViewModel
import com.kksionek.gdzietentramwaj.makeExhaustive
import com.kksionek.gdzietentramwaj.map.view.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_favorite.*

private const val COLUMN_COUNT = 7

@AndroidEntryPoint
class FavoriteFragment : Fragment() {

    private val viewModel: FavoriteLinesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_favorite, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val favAdapter = FavoritesAdapter {
            viewModel.setTramFavorite(it.lineId, !it.isFavorite)
        }
        favorites_lines_recyclerview.apply {
            layoutManager = GridLayoutManager(view.context, COLUMN_COUNT)
            addItemDecoration(SpacesItemDecoration(view.context, R.dimen.grid_offset))
            itemAnimator = object : DefaultItemAnimator() {
                override fun getChangeDuration(): Long = 100
            }
            adapter = favAdapter
        }
        favorites_error_button.setOnClickListener {
            viewModel.forceReloadFavorites()
        }

        viewModel.favoriteTrams
            .observeNonNull(viewLifecycleOwner) {
                when (it) {
                    is UiState.Success -> {
                        favorites_progress.visibility = View.GONE
                        favorites_error_view_constraintlayout.visibility = View.GONE
                        favorites_success_view_constraintlayout.visibility = View.VISIBLE
                        favAdapter.submitList(it.data)
                    }
                    is UiState.Error -> {
                        favorites_progress.visibility = View.GONE
                        favorites_error_view_constraintlayout.visibility = View.VISIBLE
                        favorites_success_view_constraintlayout.visibility = View.GONE
                        favorites_error_textview.text = getString(it.message, it.args)
                    }
                    is UiState.InProgress -> {
                        favorites_progress.visibility = View.VISIBLE
                        favorites_error_view_constraintlayout.visibility = View.GONE
                        favorites_success_view_constraintlayout.visibility = View.GONE
                    }
                }.makeExhaustive
            }
    }
}