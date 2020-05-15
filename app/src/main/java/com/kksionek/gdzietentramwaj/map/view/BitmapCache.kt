package com.kksionek.gdzietentramwaj.map.view

import com.google.android.gms.maps.model.BitmapDescriptor

interface BitmapCache {

    fun getBitmap(
        line: String,
        isTram: Boolean,
        isOldIconSetEnabled: Boolean
    ): BitmapDescriptor

    fun clearCache()
}