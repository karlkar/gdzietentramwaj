package com.kksionek.gdzietentramwaj.main.repository

import android.app.Activity
import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.Tasks
import io.reactivex.Single

class AppUpdateRepositoryImpl(
    context: Context
) : AppUpdateRepository {

    private val updateManager = AppUpdateManagerFactory.create(context)

    private lateinit var appUpdateInfo: AppUpdateInfo

    override fun isUpdateAvailable(): Single<Boolean> {
        return Single.fromCallable { Tasks.await(updateManager.appUpdateInfo) }
            .doOnSuccess { appUpdateInfo = it }
            .map { updateInfo ->
                updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        && updateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            }
    }

    override fun isUpdateInProgress(): Single<Boolean> =
        Single.fromCallable { Tasks.await(updateManager.appUpdateInfo) }
            .doOnSuccess { appUpdateInfo = it }
            .map { updateInfo ->
                updateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            }

    override fun startUpdateFlowForResult(
        appUpdateType: Int,
        activity: Activity,
        requestCode: Int
    ) {
        if (!this::appUpdateInfo.isInitialized) {
            throw IllegalStateException("AppUpdateInfo must be obtained first")
        }
        updateManager.startUpdateFlowForResult(appUpdateInfo, appUpdateType, activity, requestCode)
    }
}