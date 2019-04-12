package com.kksionek.gdzietentramwaj.map.dataSource.krakow

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleData

data class TtssVehicle(
    @SerializedName("isDeleted")
    val isDeleted: Boolean = false,
    @SerializedName("id")
    val id: String,
//    nazwa - numer + kierunek
    @SerializedName("name")
    val name: String,
    @SerializedName("latitude")
    val latitude: Int,
    @SerializedName("longitude")
    val longitude: Int
//    kolor zawsze 0x00000
//    @SerializedName("color")
//    val color: String,
//    kierunek ruchu
//    @SerializedName("heading")
//    val heading: Int,
//    identyfikator trasy
//    @SerializedName("tripId")
//    val tripId: String,
//    kategoria - tram/bus
//    @SerializedName("category")
//    val category: String,
//    ścieżka?
//    @SerializedName("path")
//    val path: List<Path>
)

fun TtssVehicle.line() = name.split(" ")[0]
fun TtssVehicle.latLng() = LatLng(latitude.toDouble() / 3600000.0, longitude.toDouble() / 3600000.0)
fun TtssVehicle.isTram() = name.length < 3 //TODO Check it

fun TtssVehicle.toVehicleData(time: String) =
    VehicleData(
        id,
        time,
        latLng(),
        line(),
        isTram(),
        null
    )