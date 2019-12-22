package com.kksionek.gdzietentramwaj.map.model

import kotlin.reflect.KClass

interface XmlDeserializer {

    @Throws(Exception::class)
    fun <T : Any> deserialize(xmlDocument: String, clazz: KClass<T>): T
}