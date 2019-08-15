package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import com.google.android.gms.tasks.Tasks
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

private const val REMOTE_CONFIG_WARSAW_API_KEY = "warsaw_api_key"

class WarsawApikeyRepositoryImpl : WarsawApikeyRepository {

    private val firebaseRemoteConfig: Single<FirebaseRemoteConfig> =
        Single.fromCallable {
            FirebaseRemoteConfig.getInstance().apply {
                Tasks.await(fetch().continueWith { activate() })
            }
        }

    override val apikey: Single<String> = firebaseRemoteConfig
        .subscribeOn(Schedulers.io())
        .cache()
        .map {
            it.getString(REMOTE_CONFIG_WARSAW_API_KEY)
        }
        .onErrorReturnItem("***REMOVED***")
}