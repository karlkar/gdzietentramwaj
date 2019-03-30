package com.kksionek.gdzietentramwaj.favorite.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.base.dataSource.FavoriteTram
import com.kksionek.gdzietentramwaj.base.viewModel.ViewModelFactory
import com.kksionek.gdzietentramwaj.favorite.viewModel.FavoriteLinesViewModel
import kotlinx.android.synthetic.main.fragment_favorite.*
import javax.inject.Inject

private const val COLUMN_COUNT = 7

class FavoriteFragment : Fragment(), OnBackPressedCallback {

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: FavoriteLinesViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_favorite, container, false)

    override fun onAttach(context: Context) {
        super.onAttach(context)

        (context.applicationContext as TramApplication).appComponent.inject(this)
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)[FavoriteLinesViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = FavoritesAdapter {
            viewModel.setTramFavorite(it.lineId, !it.isFavorite)
        }
        gridView.layoutManager = GridLayoutManager(view.context, COLUMN_COUNT)
        gridView.addItemDecoration(SpacesItemDecoration(view.context, R.dimen.grid_offset))
        gridView.itemAnimator = object : DefaultItemAnimator() {
            override fun getChangeDuration(): Long = 100
        }
        gridView.adapter = adapter
        viewModel.favoriteTrams
            .observe(this, Observer<List<FavoriteTram>> { adapter.submitList(it) })
    }

    override fun handleOnBackPressed(): Boolean = findNavController().navigateUp()
}