package com.kksionek.gdzietentramwaj.favorite.view

import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.base.dataSource.FavoriteTram
import com.kksionek.gdzietentramwaj.base.view.BaseAdapter
import com.kksionek.gdzietentramwaj.base.view.OnItemClickListener
import kotlinx.android.synthetic.main.item_favorite_line.*
import java.util.Collections

class FavoritesAdapter(onItemClickListener: OnItemClickListener<FavoriteTram>) :
    BaseAdapter<FavoriteTram, FavoritesAdapter.ViewHolder>(
        onItemClickListener,
        FavoriteDiffCallback()
    ) {

    class ViewHolder(
        parent: FavoritesAdapter,
        view: View
    ) : BaseAdapter.ViewHolder<FavoriteTram>(parent, view) {

        override fun bind(data: FavoriteTram) {
            tramNum.text = data.lineId
            tramNum.isSelected = data.isFavorite
        }
    }

    class FavoriteDiffCallback : DiffUtil.ItemCallback<FavoriteTram>() {
        override fun areItemsTheSame(oldItem: FavoriteTram, newItem: FavoriteTram): Boolean =
            oldItem.lineId == newItem.lineId

        override fun areContentsTheSame(oldItem: FavoriteTram, newItem: FavoriteTram): Boolean =
            oldItem.lineId == newItem.lineId
                    && oldItem.isFavorite == newItem.isFavorite
    }

    override fun submitList(list: List<FavoriteTram>?) {
        Collections.sort(
            list,
            NaturalOrderComparator()
        )
        super.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_line, parent, false)
        return ViewHolder(this, view)
    }
}