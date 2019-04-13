package com.kksionek.gdzietentramwaj.map.dataSource.szczecin

import com.google.gson.annotations.SerializedName

data class SzczecinVehicle(
//    @SerializedName("gmvid")
//    val gmvid: Int,
    @SerializedName("id")
    val id: String,
    @SerializedName("linia")
    val line: String,
    @SerializedName("brygada")
    val brigade: String,
    @SerializedName("typlinii")
    val lineType: String, // first letter determines if tram('t') or bus('a')
//    @SerializedName("pojazd")
//    val pojazd: String,
//    @SerializedName("trasa")
//    val trasa: String,
//    @SerializedName("kierunek")
//    val kierunek: String,
//    @SerializedName("z")
//    val z: String,
//    @SerializedName("do")
//    val _do: String,
    @SerializedName("lat")
    val lat: String,
    @SerializedName("lon")
    val lng: String
//    @SerializedName("predkosc")
//    val predkosc: String,
//    @SerializedName("punktualnosc1")
//    val punktualnosc1: String,
//    @SerializedName("punktualnosc2")
//    val punktualnosc2: String,
//    @SerializedName("ikonka")
//    val ikonka: String
)