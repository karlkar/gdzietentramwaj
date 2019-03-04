package com.kksionek.gdzietentramwaj.map.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.base.view.BaseAdapter
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesEntity
import kotlinx.android.synthetic.main.item_difficulty.*

class DifficultiesAdapter : BaseAdapter<DifficultiesEntity, DifficultiesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseAdapter.ViewHolder<DifficultiesEntity> {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_difficulty, parent, false)
        return ViewHolder(this, view)
    }

    class ViewHolder(
        parent: BaseAdapter<DifficultiesEntity, *>,
        view: View
    ) : BaseAdapter.ViewHolder<DifficultiesEntity>(parent, view) {

        init {
            recyclerview_difficulty_icons.adapter = DifficultyIconsAdapter()
        }

        override fun bind(data: DifficultiesEntity) {
            (recyclerview_difficulty_icons.adapter as DifficultyIconsAdapter).submitList(data.iconUrl)
            textview_difficulty_description.text = data.msg
            button_difficulty_link.setOnClickListener { }
        }
    }
}