package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "rss", strict = false)
data class WarsawDifficultyRss @JvmOverloads constructor(
    @param:Element(name="channel")
    @field:Element(name="channel")
    val channel: WarsawDifficultyChannel = WarsawDifficultyChannel()
)

@Root(name = "channel", strict = false)
data class WarsawDifficultyChannel @JvmOverloads constructor(
    @param:ElementList(
        inline = true,
        required = true
    )
    @field:ElementList(
        inline = true,
        required = true
    ) val items: List<Item> = mutableListOf()
)

@Root(name = "item", strict = false)
data class Item @JvmOverloads constructor(
    @field:Element(name="title")
    var title: String = "",
    @field:Element(name="link")
    var link: String = ""
)