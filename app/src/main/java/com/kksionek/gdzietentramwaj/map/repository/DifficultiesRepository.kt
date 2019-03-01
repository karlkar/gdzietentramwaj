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
private val iconPattern = "<img src=\"(.*?)\"".toRegex()

class DifficultiesRepository @Inject constructor(
    private val difficultiesInterface: DifficultiesInterface
) {

    fun getDifficulties(): Single<NetworkOperationResult<List<DifficultiesEntity>>> =
        difficultiesInterface.getDifficulties()
            .subscribeOn(Schedulers.io())
            .map { result ->
                pattern.findAll(result)
                    .map {
                        val iconList: List<String>
                        it.groupValues[1].let {
                            iconList = iconPattern.findAll(it)
                                .map { it.groupValues[1] }
                                .toList()
                        }
                        val period = it.groupValues[2]
                        val msg = it.groupValues[3]
                        val link = it.groupValues[4]
                        DifficultiesEntity(iconList, period, msg, link)
                    }
                    .ifEmpty { throw IllegalArgumentException("HTML parsing failed") }
                    .toList()
            }
            .toNetworkOperationResult()
}