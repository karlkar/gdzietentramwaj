package com.kksionek.gdzietentramwaj.base.dataSource

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class FavoriteTram(
    @PrimaryKey
    var lineId: String,
    var isFavorite: Boolean
)
