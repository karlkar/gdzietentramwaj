package com.kksionek.gdzietentramwaj.map.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.base.view.BaseAdapter
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_difficulty_icon.*

class DifficultyIconsAdapter : BaseAdapter<String, DifficultyIconsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseAdapter.ViewHolder<String> {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_difficulty_icon, parent, false)
        return ViewHolder(this, view)
    }

    class ViewHolder(
        parent: BaseAdapter<String, *>,
        view: View
    ) : BaseAdapter.ViewHolder<String>(parent, view) {

        override fun bind(data: String) {
            Picasso.get().load(data).into(imageview_difficulty_icon)
            // TODO inject picasso with dagger
        }
    }
}