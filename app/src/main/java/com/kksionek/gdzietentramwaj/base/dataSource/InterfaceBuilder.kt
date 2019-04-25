package com.kksionek.gdzietentramwaj.base.dataSource

import kotlin.reflect.KClass

interface InterfaceBuilder {
    fun <T: Any> create(baseUrl: String, klass: KClass<T>): T
}