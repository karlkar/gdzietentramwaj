package com.kksionek.gdzietentramwaj.dataSource.room

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class FavoriteTram(
    @PrimaryKey
    var lineId: String,
    var isFavorite: Boolean
)
// TODO remove this code
//{
//    override fun toString(): String = lineId
//}
