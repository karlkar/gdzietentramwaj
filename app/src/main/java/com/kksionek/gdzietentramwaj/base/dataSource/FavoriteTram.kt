package com.kksionek.gdzietentramwaj.base.dataSource

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FavoriteTram(
    @PrimaryKey
    var lineId: String,
    var isFavorite: Boolean
)
