package com.kksionek.gdzietentramwaj.map.dataSource.szczecin

import io.reactivex.Single
import retrofit2.http.GET

interface SzczecinVehicleInterface {
    // https://www.zditm.szczecin.pl/json/pojazdy.inc.php

    @GET("/json/pojazdy.inc.php")
    fun vehicles(): Single<List<SzczecinVehicle>>
}