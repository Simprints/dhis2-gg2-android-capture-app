package org.dhis2.data.biometrics

import org.dhis2.usescases.biometrics.entities.BiometricsConfig
import org.hisp.dhis.android.core.arch.api.HttpServiceClient

class BiometricsConfigApi (private val client: HttpServiceClient) {
    suspend fun getData(): List<BiometricsConfig> {
        return client.get {
            url("dataStore/simprints/config")
        }
    }
}