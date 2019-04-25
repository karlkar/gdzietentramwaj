package com.kksionek.gdzietentramwaj.base.dataSource

import retrofit2.Retrofit
import kotlin.reflect.KClass

class InterfaceBuilderImpl(
    private val retrofitBuilder: Retrofit.Builder
): InterfaceBuilder {

    override fun <T : Any> create(baseUrl: String, klass: KClass<T>): T =
        retrofitBuilder
            .baseUrl(baseUrl)
            .build()
            .create(klass.java)
}