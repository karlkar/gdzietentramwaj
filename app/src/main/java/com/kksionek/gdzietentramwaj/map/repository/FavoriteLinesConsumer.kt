package com.kksionek.gdzietentramwaj.map.repository

import com.kksionek.gdzietentramwaj.base.dataSource.FavoriteTram
import com.kksionek.gdzietentramwaj.base.dataSource.TramDao
import com.kksionek.gdzietentramwaj.map.dataSource.TramData
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
