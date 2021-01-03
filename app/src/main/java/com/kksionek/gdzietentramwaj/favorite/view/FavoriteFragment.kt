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
import com.kksionek.gdzietentramwaj.databinding.FragmentFavoriteBinding
import com.kksionek.gdzietentramwaj.favorite.viewModel.FavoriteLinesViewModel
import com.kksionek.gdzietentramwaj.makeExhaustive
import com.kksionek.gdzietentramwaj.map.view.UiState
import dagger.hilt.android.AndroidEntryPoint

private const val COLUMN_COUNT = 7

@AndroidEntryPoint
class FavoriteFragment : Fragment() {

    private val viewModel: FavoriteLinesViewModel by viewModels()

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentFavoriteBinding.inflate(inflater, container, false)
        .also { _binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val favAdapter = FavoritesAdapter {
            viewModel.setTramFavorite(it.lineId, !it.isFavorite)
        }
        binding.favoritesLinesRecyclerview.apply {
            layoutManager = GridLayoutManager(view.context, COLUMN_COUNT)
            addItemDecoration(SpacesItemDecoration(view.context, R.dimen.grid_offset))
            itemAnimator = object : DefaultItemAnimator() {
                override fun getChangeDuration(): Long = 100
            }
            adapter = favAdapter
        }
        binding.favoritesErrorButton.setOnClickListener {
            viewModel.forceReloadFavorites()
        }

        viewModel.favoriteTrams
            .observeNonNull(viewLifecycleOwner) {
                when (it) {
                    is UiState.Success -> {
                        binding.favoritesProgress.visibility = View.GONE
                        binding.favoritesErrorViewConstraintlayout.visibility = View.GONE
                        binding.favoritesSuccessViewConstraintlayout.visibility = View.VISIBLE
                        favAdapter.submitList(it.data)
                    }
                    is UiState.Error -> {
                        binding.favoritesProgress.visibility = View.GONE
                        binding.favoritesErrorViewConstraintlayout.visibility = View.VISIBLE
                        binding.favoritesSuccessViewConstraintlayout.visibility = View.GONE
                        binding.favoritesErrorTextview.text = getString(it.message, it.args)
                    }
                    is UiState.InProgress -> {
                        binding.favoritesProgress.visibility = View.VISIBLE
                        binding.favoritesErrorViewConstraintlayout.visibility = View.GONE
                        binding.favoritesSuccessViewConstraintlayout.visibility = View.GONE
                    }
                }.makeExhaustive
            }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}