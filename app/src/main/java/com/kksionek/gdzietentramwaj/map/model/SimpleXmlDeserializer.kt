package com.kksionek.gdzietentramwaj.map.model

import org.simpleframework.xml.core.Persister
import kotlin.reflect.KClass

class SimpleXmlDeserializer(private val persister: Persister) : XmlDeserializer {

    override fun <T : Any> deserialize(xmlDocument: String, clazz: KClass<T>): T =
        persister.read(clazz.java, xmlDocument)
}