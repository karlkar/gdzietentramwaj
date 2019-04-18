package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesEntity
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesState
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class WarsawDifficultiesDataSource(
    private val warsawDifficultiesInterface: WarsawDifficultiesInterface
) : DifficultiesDataSource {

    override fun getDifficulties(): Single<DifficultiesState> =
        warsawDifficultiesInterface.getDifficulties()
            .subscribeOn(Schedulers.io())
            .map { result ->
                if (result.isEmpty()) {
                    DifficultiesState(true, emptyList())
                } else {
                    val difficultiesList = pattern.findAll(result)
                        .map { matchResult ->
                            val id = matchResult.groupValues[1]
                            val iconList: List<String>
                            matchResult.groupValues[2].let { allIconsStr ->
                                iconList = singleIconPattern.findAll(allIconsStr)
                                    .map { it.groupValues[1] }
                                    .map { it.replaceFirst(".", "https://www.ztm.waw.pl/") }
                                    .toList()
                            }
//                            val period = matchResult.groupValues[3]
                            val msg = matchResult.groupValues[4]
                            val link = "https://www.ztm.waw.pl" + matchResult.groupValues[5]
                                .replace("&amp;", "&") + "&i=$id"
                            DifficultiesEntity(iconList, msg, link)
                        }
                        .ifEmpty { throw IllegalArgumentException("HTML parsing failed") }
                        .toList()
                    DifficultiesState(true, difficultiesList)
                }
            }

    companion object {

        private val pattern =
            "<tr.*?id=\"komunikat_(.*?)_.*?(<img src=\".*?\".*?)?okres\">(.*?)<\\/span.*?zmiana\">(.*?)<span.*?href=\".(.*?)\"".toRegex()
        private val singleIconPattern = "<img src=\"(.*?)\"".toRegex()
    }
}