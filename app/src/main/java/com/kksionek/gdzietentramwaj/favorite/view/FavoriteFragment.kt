package com.kksionek.gdzietentramwaj.favorite.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.base.observeNonNull
import com.kksionek.gdzietentramwaj.base.viewModel.ViewModelFactory
import com.kksionek.gdzietentramwaj.favorite.viewModel.FavoriteLinesViewModel
import com.kksionek.gdzietentramwaj.makeExhaustive
import com.kksionek.gdzietentramwaj.map.view.UiState
import kotlinx.android.synthetic.main.fragment_favorite.*
import javax.inject.Inject

private const val COLUMN_COUNT = 7

class FavoriteFragment : Fragment() {

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: FavoriteLinesViewModel by viewModels(factoryProducer = { viewModelFactory })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_favorite, container, false)

    override fun onAttach(context: Context) {
        super.onAttach(context)

        (context.applicationContext as TramApplication).appComponent.inject(this)
    }

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