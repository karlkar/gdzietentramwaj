package com.kksionek.gdzietentramwaj.favorite.view

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.base.dataSource.FavoriteTram
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.grid_favorite_element.*
import java.util.Collections

class FavoritesAdapter(
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {

    class ViewHolder(
        parent: FavoritesAdapter,
        view: View
    ) : RecyclerView.ViewHolder(view), LayoutContainer {

        private lateinit var innerData: FavoriteTram

        override val containerView: View?
            get() = itemView

        init {
            itemView.setOnClickListener { parent.onItemClickListener.invoke(innerData) }
        }

        fun bind(data: FavoriteTram) {
            innerData = data
            tramNum.text = data.lineId
            tramNum.isSelected = data.isFavorite
        }
    }

    private val trams = mutableListOf<FavoriteTram>()

    class FavoriteDiffCallback(
        private val oldItems: List<FavoriteTram>,
        private val newItems: List<FavoriteTram>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean =
            oldItems[oldItem].lineId == newItems[newItem].lineId

        override fun getOldListSize(): Int = oldItems.size

        override fun getNewListSize(): Int = newItems.size

        override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean =
            oldItems[oldItem].lineId == newItems[newItem].lineId
                    && oldItems[oldItem].isFavorite == newItems[newItem].isFavorite
    }

    fun setData(data: List<FavoriteTram>) {
        Collections.sort(
            data,
            NaturalOrderComparator()
        )
        val diffResult = DiffUtil.calculateDiff(FavoriteDiffCallback(trams, data))
        trams.clear()
        trams.addAll(data)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.grid_favorite_element, parent, false)
        return ViewHolder(this, view)
    }

    override fun getItemCount(): Int = trams.size

    override fun onBindViewHolder(holder: ViewHolder, viewType: Int) {
        holder.bind(trams[holder.adapterPosition])
    }
}

typealias OnItemClickListener = (FavoriteTram) -> Unit