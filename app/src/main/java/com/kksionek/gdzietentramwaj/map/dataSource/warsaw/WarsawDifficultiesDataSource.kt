package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import com.kksionek.gdzietentramwaj.map.model.DifficultiesEntity
import com.kksionek.gdzietentramwaj.map.model.DifficultiesState
import com.kksionek.gdzietentramwaj.map.model.XmlDeserializer
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class WarsawDifficultiesDataSource(
    private val warsawDifficultiesInterface: WarsawDifficultiesInterface,
    private val xmlDeserializer: XmlDeserializer
) : DifficultiesDataSource {

    override fun getDifficulties(): Single<DifficultiesState> {
        return warsawDifficultiesInterface.getDifficulties()
            .subscribeOn(Schedulers.io())
            .map { result ->
                if (result.isEmpty()) {
                    DifficultiesState(true, emptyList())
                } else {
                    val channel = xmlDeserializer.deserialize(result, WarsawDifficultyRss::class)
                        .channel
                        .items
                        .map {
                            DifficultiesEntity(
                                null,
                                it.title,
                                it.link
                            )
                        }
                        .ifEmpty { throw IllegalArgumentException("HTML parsing failed") }

                    DifficultiesState(true, channel)
                }
            }
    }
}