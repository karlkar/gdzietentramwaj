package com.kksionek.gdzietentramwaj.base.dataSource.room

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class FavoriteTram(
    @PrimaryKey
    var lineId: String,
    var isFavorite: Boolean
)
