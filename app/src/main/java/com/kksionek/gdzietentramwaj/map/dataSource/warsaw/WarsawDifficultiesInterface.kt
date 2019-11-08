package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import io.reactivex.Single
import retrofit2.http.GET

interface WarsawDifficultiesInterface {

    @GET("/feed/?post_type=impediment")
    fun getDifficulties(): Single<String>
}