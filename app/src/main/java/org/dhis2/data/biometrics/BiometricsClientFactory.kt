package org.dhis2.data.biometrics

import android.content.Context
import org.dhis2.BuildConfig
import org.dhis2.commons.biometrics.BiometricsPreference.Companion.CONFIDENCE_SCORE_FILTER
import org.dhis2.commons.biometrics.BiometricsPreference.Companion.PROJECT_ID
import org.dhis2.commons.prefs.BasicPreferenceProviderImpl
import org.dhis2.data.biometrics.biometricsClient.BiometricsClient
import org.hisp.dhis.android.core.D2Manager

object BiometricsClientFactory {
    fun get(context: Context): BiometricsClient {
        val basicPreferences = BasicPreferenceProviderImpl(context)

        val projectId = basicPreferences.getString(PROJECT_ID, "Ma9wi0IBdo215PKRXOf5")!!
        val username = getUsername()
        val confidenceScoreFilter = basicPreferences.getInt(CONFIDENCE_SCORE_FILTER, 0)

        return BiometricsClient(
            projectId,
            username,
            confidenceScoreFilter,
            BuildConfig.VERSION_NAME
        )
    }

    private fun getUsername(): String {
        val user = D2Manager.getD2().userModule().user().blockingGet()

        val userId = user?.username() ?: "admin"
        return userId
    }
}