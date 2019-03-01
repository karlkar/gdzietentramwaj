package com.kksionek.gdzietentramwaj.map.dataSource

import io.reactivex.Single
import retrofit2.http.GET

interface DifficultiesInterface {

    @GET("/getUtrudnienia.php")
    fun getDifficulties(): Single<String>
}