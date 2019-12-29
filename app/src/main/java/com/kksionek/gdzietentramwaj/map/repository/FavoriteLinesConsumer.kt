package com.kksionek.gdzietentramwaj.map.repository

import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.base.dataSource.FavoriteTram
import com.kksionek.gdzietentramwaj.base.dataSource.TramDao
import com.kksionek.gdzietentramwaj.map.model.VehicleData
import io.reactivex.functions.Consumer

class FavoriteLinesConsumer(
    private val tramDao: TramDao,
    private val selectedCity: Cities
) : Consumer<List<VehicleData>> {

    @Throws(Exception::class)
    override fun accept(tramDataMap: List<VehicleData>) {
        tramDataMap
            .map { it.line }
            .toSet()
            .forEach {
                tramDao.save(FavoriteTram(it, false, selectedCity.id))
            }
    }
}
