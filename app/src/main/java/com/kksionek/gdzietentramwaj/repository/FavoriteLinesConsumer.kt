package com.kksionek.gdzietentramwaj.repository

import com.kksionek.gdzietentramwaj.dataSource.TramData
import com.kksionek.gdzietentramwaj.dataSource.room.FavoriteTram
import com.kksionek.gdzietentramwaj.dataSource.room.TramDao

import io.reactivex.functions.Consumer

class FavoriteLinesConsumer(private val mTramDao: TramDao) : Consumer<Map<String, TramData>> {
    private val mSavedLines = mutableSetOf<String>()

    @Throws(Exception::class)
    override fun accept(tramDataMap: Map<String, TramData>) {
        tramDataMap.values
            .map { it.firstLine }
            .forEach {
                if (it !in mSavedLines) {
                    mTramDao.save(FavoriteTram(it, false))
                    mSavedLines.add(it)
                }
            }
    }
}
