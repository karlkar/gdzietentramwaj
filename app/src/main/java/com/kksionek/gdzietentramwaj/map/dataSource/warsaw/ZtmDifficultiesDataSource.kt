package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesEntity
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class ZtmDifficultiesDataSource(
    private val ztmDifficultiesInterface: ZtmDifficultiesInterface
) : DifficultiesDataSource {

    override fun isAvailable(): Boolean = true

    override fun getDifficulties(): Single<List<DifficultiesEntity>> =
        ztmDifficultiesInterface.getDifficulties()
            .subscribeOn(Schedulers.io())
            .map { result ->
                if (result.isEmpty()) {
                    emptyList()
                } else {
                    pattern.findAll(result)
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
                }
            }

    companion object {

        private val pattern =
            "<tr.*?id=\"komunikat_(.*?)_.*?(<img src=\".*?\".*?)okres\">(.*?)<\\/span.*?zmiana\">(.*?)<span.*?href=\".(.*?)\"".toRegex()
        private val singleIconPattern = "<img src=\"(.*?)\"".toRegex()
    }
}