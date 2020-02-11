package com.kksionek.gdzietentramwaj.main.repository

import android.app.Activity
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.Tasks
import io.reactivex.Single

// TODO: Implement Android test using FakeAppUpdateRepository
class AppUpdateRepositoryImpl(private val updateManager: AppUpdateManager) : AppUpdateRepository {

    private lateinit var appUpdateInfo: AppUpdateInfo

    override fun isUpdateAvailable(): Single<Boolean> {
        return Single.fromCallable { Tasks.await(updateManager.appUpdateInfo) }
            .doOnSuccess { appUpdateInfo = it }
            .map { updateInfo ->
                updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        && updateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            }
    }

    override fun isUpdateInProgress(): Single<Boolean> {
        return Single.fromCallable { Tasks.await(updateManager.appUpdateInfo) }
            .doOnSuccess { appUpdateInfo = it }
            .map { updateInfo ->
                updateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            }
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