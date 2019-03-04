package com.kksionek.gdzietentramwaj.map.repository

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesEntity
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesInterface
import com.kksionek.gdzietentramwaj.map.dataSource.NetworkOperationResult
import com.kksionek.gdzietentramwaj.toNetworkOperationResult
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

private val pattern =
    "<tr.*?(<img src=\".*?\".*?)okres\">(.*?)<\\/span.*?zmiana\">(.*?)<span.*?href=\".(.*?)\"".toRegex()
private val singleIconPattern = "<img src=\"(.*?)\"".toRegex()

class DifficultiesRepository @Inject constructor(
    private val difficultiesInterface: DifficultiesInterface
) {

    fun getDifficulties(): Single<NetworkOperationResult<List<DifficultiesEntity>>> =
        difficultiesInterface.getDifficulties()
            .subscribeOn(Schedulers.io())
            .map { result ->
                pattern.findAll(result)
                    .map { matchResult ->
                        val iconList: List<String>
                        matchResult.groupValues[1].let { allIconsStr ->
                            iconList = singleIconPattern.findAll(allIconsStr)
                                .map { it.groupValues[1] }
                                .map { it.replaceFirst(".", "https://www.ztm.waw.pl/") }
                                .toList()
                        }
                        val period = matchResult.groupValues[2]
                        val msg = matchResult.groupValues[3]
                        val link = matchResult.groupValues[4]
                        DifficultiesEntity(iconList, period, msg, link)
                    }
                    .ifEmpty { throw IllegalArgumentException("HTML parsing failed") }
                    .toList()
            }
            .toNetworkOperationResult()
}