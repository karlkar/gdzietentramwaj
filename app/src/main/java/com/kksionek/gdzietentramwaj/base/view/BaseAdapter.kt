package com.kksionek.gdzietentramwaj.base.view

import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<T : Any, VH : BaseAdapter.ViewHolder<T>>(
    protected val onItemClickListener: OnItemClickListener<T>? = null,
    diffCallback: DiffUtil.ItemCallback<T> = DefaultDiffCallback()
) : ListAdapter<T, BaseAdapter.ViewHolder<T>>(diffCallback) {

    abstract class ViewHolder<T : Any>(
        parent: BaseAdapter<T, *>,
        view: View
    ) : RecyclerView.ViewHolder(view) {

        private lateinit var innerData: T

        var data: T
            get() = innerData
            internal set(value) {
                innerData = value
                bind(value)
            }

        init {
            itemView.setOnClickListener { parent.onItemClickListener?.invoke(innerData) }
        }

        protected abstract fun bind(data: T)
    }

    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
        holder.data = getItem(position)
    }

    class DefaultDiffCallback<T: Any> : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(p0: T, p1: T): Boolean = p0 === p1

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(p0: T, p1: T): Boolean = p0 == p1
    }
}

typealias OnItemClickListener<T> = (T) -> Unit