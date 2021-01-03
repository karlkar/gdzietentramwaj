package com.kksionek.gdzietentramwaj.favorite.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.kksionek.gdzietentramwaj.base.dataSource.FavoriteTram
import com.kksionek.gdzietentramwaj.base.view.BaseAdapter
import com.kksionek.gdzietentramwaj.base.view.OnItemClickListener
import com.kksionek.gdzietentramwaj.databinding.ItemFavoriteLineBinding
import java.util.*

class FavoritesAdapter(onItemClickListener: OnItemClickListener<FavoriteTram>) :
    BaseAdapter<FavoriteTram, FavoritesAdapter.ViewHolder>(
        onItemClickListener,
        FavoriteDiffCallback()
    ) {

    class ViewHolder(
        parent: FavoritesAdapter,
        private val binding: ItemFavoriteLineBinding
    ) : BaseAdapter.ViewHolder<FavoriteTram>(parent, binding.root) {

        override fun bind(data: FavoriteTram) {
            with(binding.tramNum) {
                text = data.lineId
                isSelected = data.isFavorite
            }
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
        val mutableList = list?.toMutableList() ?: mutableListOf()
        Collections.sort(
            mutableList,
            NaturalOrderComparator()
        )
        super.submitList(mutableList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemFavoriteLineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(this, binding)
    }
}