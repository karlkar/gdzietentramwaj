package com.kksionek.gdzietentramwaj.favorite.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.GridView
import android.widget.TextView
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.base.dataSource.room.FavoriteTram
import com.kksionek.gdzietentramwaj.base.viewModel.ViewModelFactory
import com.kksionek.gdzietentramwaj.favorite.viewModel.FavoriteLinesActivityViewModel
import java.util.Collections
import javax.inject.Inject

class FavoriteLinesActivity : AppCompatActivity() {

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory

    private lateinit var mViewModel: FavoriteLinesActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_lines)

        val myToolbar = findViewById<Toolbar>(R.id.my_toolbar)
        setSupportActionBar(myToolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        myToolbar.setNavigationOnClickListener { onBackPressed() }

        val gridView = findViewById<GridView>(R.id.gridView)

        (application as TramApplication).appComponent.inject(this)

        mViewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(FavoriteLinesActivityViewModel::class.java)

        val adapter = FavoritesAdapter()
        gridView.adapter = adapter
        mViewModel.favoriteTrams
            .observe(this, Observer<List<FavoriteTram>> { adapter.setData(it!!) })
    }

    private inner class FavoritesAdapter internal constructor() : // TODO Use recyclerview
        ArrayAdapter<FavoriteTram>(this@FavoriteLinesActivity, R.layout.grid_favorite_element) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: View
            val holder: ViewHolder = if (convertView == null) {
                view = LayoutInflater.from(this@FavoriteLinesActivity).inflate(
                    R.layout.grid_favorite_element,
                    parent,
                    false
                )!!

                val viewHolder = ViewHolder()
                viewHolder.textView = view.findViewById(R.id.tramNum)
                view.tag = viewHolder
                viewHolder
            } else {
                view = convertView
                view.tag as ViewHolder
            }

            val tramData = getItem(position) ?: return view
            holder.textView.text = tramData.lineId
            if (tramData.isFavorite) {
                view.setBackgroundResource(R.color.favoriteLineColor)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    view.background = null
                } else {
                    view.setBackgroundDrawable(null)
                }
            }

            view.setOnClickListener {
                mViewModel.setTramFavorite(tramData.lineId, !tramData.isFavorite)
            }
            return view
        }

        fun setData(favoriteTrams: List<FavoriteTram>) {
            clear()
            Collections.sort(favoriteTrams,
                NaturalOrderComparator()
            )
            addAll(favoriteTrams)
        }

        private inner class ViewHolder {
            internal lateinit var textView: TextView
        }
    }
}
