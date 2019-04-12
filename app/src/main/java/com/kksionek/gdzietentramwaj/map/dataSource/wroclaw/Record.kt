package com.kksionek.gdzietentramwaj.map.dataSource.wroclaw

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName

data class Record(
    @SerializedName("Ostatnia_Pozycja_Dlugosc")
    val longitude: Double,
    @SerializedName("_id")
    val id: Int,
    @SerializedName("Nazwa_Linii")
    val line: String,
    @SerializedName("Brygada")
    val brigade: String,
//    @SerializedName("Nr_Rej")
//    val nrRej: String,
    @SerializedName("Data_Aktualizacji")
    val timestamp: String,
//    @SerializedName("Nr_Boczny")
//    val nrBoczny: String,
    @SerializedName("Ostatnia_Pozycja_Szerokosc")
    val latitude: Double
) {

    val latLng: LatLng
        get() = LatLng(latitude, longitude)
}