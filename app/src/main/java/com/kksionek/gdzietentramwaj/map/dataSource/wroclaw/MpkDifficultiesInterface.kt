package com.kksionek.gdzietentramwaj.map.dataSource.wroclaw

import io.reactivex.Single
import retrofit2.http.GET

interface MpkDifficultiesInterface {
    // http://mpk.wroc.pl/informacje/zmiany-w-komunikacji

    @GET("/informacje/zmiany-w-komunikacji")
    fun getDifficulties(): Single<String>
}