package com.kksionek.gdzietentramwaj.map.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.recyclerview.widget.DiffUtil
import com.kksionek.gdzietentramwaj.base.view.BaseAdapter
import com.kksionek.gdzietentramwaj.base.view.ImageLoader
import com.kksionek.gdzietentramwaj.base.view.OnItemClickListener
import com.kksionek.gdzietentramwaj.databinding.ItemDifficultyBinding
import com.kksionek.gdzietentramwaj.map.model.DifficultiesEntity

class DifficultiesAdapter(
    private val imageLoader: ImageLoader,
    onItemClickListener: OnItemClickListener<DifficultiesEntity>
) :
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
        val binding =
            ItemDifficultyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(this, binding, imageLoader)
    }

    class ViewHolder(
        parent: BaseAdapter<DifficultiesEntity, *>,
        private val binding: ItemDifficultyBinding,
        imageLoader: ImageLoader
    ) : BaseAdapter.ViewHolder<DifficultiesEntity>(parent, binding.root) {

        init {
            binding.recyclerviewDifficultyIcons.adapter = DifficultyIconsAdapter(imageLoader)
        }

        override fun bind(data: DifficultiesEntity) {
            (binding.recyclerviewDifficultyIcons.adapter as DifficultyIconsAdapter).submitList(data.iconUrl)
            binding.textviewDifficultyDescription.text =
                HtmlCompat.fromHtml(data.msg, FROM_HTML_MODE_LEGACY)
        }
    }
}