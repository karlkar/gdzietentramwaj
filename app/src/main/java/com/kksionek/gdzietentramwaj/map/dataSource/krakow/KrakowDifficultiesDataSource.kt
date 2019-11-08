package com.kksionek.gdzietentramwaj.map.dataSource.krakow

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import com.kksionek.gdzietentramwaj.map.model.DifficultiesEntity
import com.kksionek.gdzietentramwaj.map.model.DifficultiesState
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class KrakowDifficultiesDataSource(
    private val krakowDifficultiesInterface: KrakowDifficultiesInterface
) : DifficultiesDataSource {

    override fun getDifficulties(): Single<DifficultiesState> =
        krakowDifficultiesInterface.getDifficulties()
            .subscribeOn(Schedulers.io())
            .map { result ->
                if (result.isEmpty()) {
                    DifficultiesState(true, emptyList())
                } else {
                    val findResult = newsBarPattern.find(result)
                    val difficultiesList = findResult?.groupValues?.get(1)?.let {
                        pattern.findAll(it)
                            .map { matchResult ->
                                val link = "http://mpk.krakow.pl" + matchResult.groupValues[1]
                                val text = matchResult.groupValues[2]
                                    .trim()
                                    .replace("<span class=\"data-gruba\">", "<b>")
                                    .replace("</span>", "</b><br/>")
                                    .replace("<br/> -", "<br/>")
                                DifficultiesEntity(
                                    null,
                                    text,
                                    link
                                )
                            }
                            .ifEmpty { throw IllegalArgumentException("HTML parsing failed") }
                            .toList()
                    }
                    DifficultiesState(
                        true,
                        difficultiesList ?: emptyList()
                    )
                }
            }

    companion object {

        private val newsBarPattern =
            "<div class=\"newsBar\"(.*?)<\\/div>".toRegex(RegexOption.DOT_MATCHES_ALL)
        private val pattern =
            "<a href=\"(\\/pl\\/import-komunikaty\\/news.*?)\">(.*?)<\\/a>".toRegex(RegexOption.DOT_MATCHES_ALL)
    }
}