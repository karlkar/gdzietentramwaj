package com.kksionek.gdzietentramwaj.map.repository

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesEntity
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesInterface
import com.kksionek.gdzietentramwaj.map.dataSource.NetworkOperationResult
import com.kksionek.gdzietentramwaj.toNetworkOperationResult
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

private val pattern =
    "<tr.*?id=\"komunikat_(.*?)_.*?(<img src=\".*?\".*?)okres\">(.*?)<\\/span.*?zmiana\">(.*?)<span.*?href=\".(.*?)\"".toRegex()
private val singleIconPattern = "<img src=\"(.*?)\"".toRegex()

class DifficultiesRepository @Inject constructor(
    private val difficultiesInterface: DifficultiesInterface
) {

    fun getDifficulties(): Observable<NetworkOperationResult<List<DifficultiesEntity>>> =
        difficultiesInterface.getDifficulties()
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
                            val period = matchResult.groupValues[3]
                            val msg = matchResult.groupValues[4]
                            val link = "https://www.ztm.waw.pl" + matchResult.groupValues[5]
                                .replace("&amp;", "&") + "&i=$id"
                            DifficultiesEntity(iconList, period, msg, link)
                        }
                        .ifEmpty { throw IllegalArgumentException("HTML parsing failed") }
                        .toList()
                }
            }
            .toNetworkOperationResult()
            .toObservable()
            .startWith(NetworkOperationResult.InProgress())
}