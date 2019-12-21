package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.kksionek.gdzietentramwaj.BuildConfig
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

@VisibleForTesting
const val REMOTE_CONFIG_WARSAW_API_KEY = "warsaw_api_key"
@VisibleForTesting
const val DEFAULT_API_KEY = BuildConfig.ZTM_API_KEY

class WarsawApikeyRepositoryImpl(
    private val firebaseRemoteConfig: FirebaseRemoteConfig
) : WarsawApikeyRepository {

    private val fetchAndActivateSingle = Single.fromPublisher<Boolean> { publisher ->
        firebaseRemoteConfig.fetchAndActivate()
            .addOnSuccessListener {
                publisher.onNext(it)
                publisher.onComplete()
            }
            .addOnCanceledListener {
                publisher.onNext(false)
                publisher.onComplete()
            }
            .addOnFailureListener {
                publisher.onError(it)
            }
    }

    private val firebaseRemoteConfigSingle: Single<FirebaseRemoteConfig> =
        fetchAndActivateSingle.map { firebaseRemoteConfig }

    override val apikey: Single<String> = firebaseRemoteConfigSingle
        .subscribeOn(Schedulers.io())
        .map {
            it.getString(REMOTE_CONFIG_WARSAW_API_KEY)
        }
        .onErrorResumeNext {
            Log.e(TAG, "Error", it) // TODO: Use Timber or even report to crashlytics
            Single.just("")
        }
        .map {
            if (it.isBlank()) DEFAULT_API_KEY else it
        }
        .cache()

    companion object {
        private const val TAG = "WarsawApikeyRepositoryI"
    }
}