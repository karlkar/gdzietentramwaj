package com.kksionek.gdzietentramwaj.base.repository

import com.kksionek.gdzietentramwaj.base.dataSource.TramData
import com.kksionek.gdzietentramwaj.base.dataSource.room.FavoriteTram
import com.kksionek.gdzietentramwaj.base.dataSource.room.TramDao

import io.reactivex.functions.Consumer
import javax.inject.Inject

class FavoriteLinesConsumer @Inject constructor(private val mTramDao: TramDao) :
    Consumer<List<TramData>> {
    private val savedLines = mutableSetOf<String>()

    @Throws(Exception::class)
    override fun accept(tramDataMap: List<TramData>) {
        tramDataMap
            .map { it.firstLine }
            .toSet()
            .filter { it !in savedLines }
            .forEach {
                mTramDao.save(FavoriteTram(it, false))
                savedLines.add(it)
            }
    }
}
