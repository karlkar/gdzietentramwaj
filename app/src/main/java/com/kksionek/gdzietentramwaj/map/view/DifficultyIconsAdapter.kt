package com.kksionek.gdzietentramwaj.map.view

import android.view.LayoutInflater
import android.view.ViewGroup
import com.kksionek.gdzietentramwaj.base.view.BaseAdapter
import com.kksionek.gdzietentramwaj.base.view.ImageLoader
import com.kksionek.gdzietentramwaj.databinding.ItemDifficultyIconBinding

class DifficultyIconsAdapter(private val imageLoader: ImageLoader) :
    BaseAdapter<String, DifficultyIconsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseAdapter.ViewHolder<String> {
        val binding = ItemDifficultyIconBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(this, binding, imageLoader)
    }

    class ViewHolder(
        parent: BaseAdapter<String, *>,
        private val binding: ItemDifficultyIconBinding,
        private val imageLoader: ImageLoader
    ) : BaseAdapter.ViewHolder<String>(parent, binding.root) {

        override fun bind(data: String) {
            imageLoader.load(data, binding.imageviewDifficultyIcon)
        }
    }
}