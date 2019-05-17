package com.kksionek.gdzietentramwaj.main.repository

import android.app.Activity
import io.reactivex.Single

interface AppUpdateRepository {

    fun isUpdateAvailable(): Single<Boolean>

    fun isUpdateInProgress(): Single<Boolean>

    fun startUpdateFlowForResult(appUpdateType: Int, activity: Activity, requestCode: Int)
}