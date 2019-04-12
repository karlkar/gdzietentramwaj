package com.kksionek.gdzietentramwaj.map.dataSource.krakow

import io.reactivex.Single
import retrofit2.http.GET

interface TtssDifficultiesInterface {

    @GET("/pl/import-komunikaty/")
    fun getDifficulties(): Single<String>
}
