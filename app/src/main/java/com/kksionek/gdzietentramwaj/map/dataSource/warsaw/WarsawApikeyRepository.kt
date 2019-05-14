package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import io.reactivex.Single

interface WarsawApikeyRepository {

    val apikey: Single<String>
}