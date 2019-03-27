package com.kksionek.gdzietentramwaj.map.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.recyclerview.widget.DiffUtil
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.base.view.BaseAdapter
import com.kksionek.gdzietentramwaj.base.view.ImageLoader
import com.kksionek.gdzietentramwaj.base.view.OnItemClickListener
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesEntity
import kotlinx.android.synthetic.main.item_difficulty.*

class DifficultiesAdapter(
    private val imageLoader: ImageLoader,
    onItemClickListener: OnItemClickListener<DifficultiesEntity>) :
    BaseAdapter<DifficultiesEntity, DifficultiesAdapter.ViewHolder>(
        onItemClickListener,
        DiffCallback()
    ) {

    class DiffCallback : DiffUtil.ItemCallback<DifficultiesEntity>() {
        override fun areItemsTheSame(p0: DifficultiesEntity, p1: DifficultiesEntity): Boolean =
            p0.link == p1.link

        override fun areContentsTheSame(p0: DifficultiesEntity, p1: DifficultiesEntity): Boolean =
            p0 == p1
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseAdapter.ViewHolder<DifficultiesEntity> {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_difficulty, parent, false)
        return ViewHolder(this, view, imageLoader)
    }

    class ViewHolder(
        parent: BaseAdapter<DifficultiesEntity, *>,
        view: View,
        imageLoader: ImageLoader
    ) : BaseAdapter.ViewHolder<DifficultiesEntity>(parent, view) {

        init {
            recyclerview_difficulty_icons.adapter = DifficultyIconsAdapter(imageLoader)
        }

        override fun bind(data: DifficultiesEntity) {
            (recyclerview_difficulty_icons.adapter as DifficultyIconsAdapter).submitList(data.iconUrl)
            textview_difficulty_description.text = HtmlCompat.fromHtml(data.msg, FROM_HTML_MODE_LEGACY)
        }
    }
}