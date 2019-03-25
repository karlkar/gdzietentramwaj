package com.kksionek.gdzietentramwaj.favorite.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.view.MenuItem
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.base.dataSource.FavoriteTram
import com.kksionek.gdzietentramwaj.base.viewModel.ViewModelFactory
import com.kksionek.gdzietentramwaj.favorite.viewModel.FavoriteLinesActivityViewModel
import kotlinx.android.synthetic.main.activity_favorite_lines.*
import javax.inject.Inject


private const val COLUMN_COUNT = 7

class FavoriteLinesActivity : AppCompatActivity() {

    // https://medium.com/androiddevelopers/testing-room-migrations-be93cdb0d975

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory

    private lateinit var mViewModel: FavoriteLinesActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_lines)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        (application as TramApplication).appComponent.inject(this)

        mViewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(FavoriteLinesActivityViewModel::class.java)

        val adapter = FavoritesAdapter {
            mViewModel.setTramFavorite(it.lineId, !it.isFavorite)
        }
        gridView.layoutManager = GridLayoutManager(this, COLUMN_COUNT)
        gridView.addItemDecoration(SpacesItemDecoration(this, R.dimen.grid_offset))
        gridView.itemAnimator = object : DefaultItemAnimator() {
            override fun getChangeDuration(): Long = 100
        }
        gridView.adapter = adapter
        mViewModel.favoriteTrams
            .observe(this, Observer<List<FavoriteTram>> { adapter.submitList(it) })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
