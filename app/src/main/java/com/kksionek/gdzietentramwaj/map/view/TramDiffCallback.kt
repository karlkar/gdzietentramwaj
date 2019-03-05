package com.kksionek.gdzietentramwaj.map.view

import android.support.v7.util.DiffUtil

class TramDiffCallback(
        private val oldList: List<TramMarker>,
        private val newList: List<TramMarker>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition].id == newList[newItemPosition].id

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition].finalPosition == newList[newItemPosition].finalPosition
                    && oldList[oldItemPosition].prevPosition == newList[newItemPosition].prevPosition
    }