package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import io.reactivex.Single
import retrofit2.http.GET

interface WarsawDifficultiesInterface {

    @GET("/getUtrudnienia.php")
    fun getDifficulties(): Single<String>
}