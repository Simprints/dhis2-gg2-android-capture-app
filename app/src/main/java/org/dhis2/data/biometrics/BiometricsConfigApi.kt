package org.dhis2.data.biometrics

import org.hisp.dhis.android.core.arch.api.HttpServiceClient

class BiometricsConfigApi (private val client: HttpServiceClient) {
    suspend fun getData(): List<BiometricsConfigDTO> {
        return client.get {
            url("dataStore/simprints/config")
        }
    }
}