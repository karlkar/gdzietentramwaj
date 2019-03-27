package com.kksionek.gdzietentramwaj.main.repository

interface VersionRepository {

    /**
     * Returns the version number that was previously launched. 0 if it is a first launch
     */
    fun getPreviouslyLaunchedVersion(): Int

    fun saveLastLaunchedVersion(version: Int)
}