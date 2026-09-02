// EyeSeeTea customization - Biometrics Configuration Selection
// Simprints behavior: the biometrics configuration must be refreshed whenever metadata is
// synced, not only at login. Baseline moved metadata sync into the KMP `:sync` module, which
// cannot see this module, so the work is registered through the `PostMetadataSyncAction`
// extension point instead. Flavor-specific file: other flavors register no actions.
package org.dhis2.di

import org.dhis2.commons.prefs.BasicPreferenceProviderImpl
import org.dhis2.data.biometrics.BiometricsConfigApi
import org.dhis2.data.biometrics.BiometricsConfigRepositoryImpl
import org.dhis2.mobile.commons.domain.PostMetadataSyncAction
import org.hisp.dhis.android.core.D2
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val postMetadataSyncModule =
    module {
        factory<List<PostMetadataSyncAction>> {
            listOf(
                PostMetadataSyncAction {
                    // Built inside the action, not in the factory: the repository is only
                    // needed when a metadata sync actually completes, and building it here
                    // avoids the lambda holding on to D2 and the Context until then.
                    val d2 = get<D2>()
                    val repository =
                        BiometricsConfigRepositoryImpl(
                            d2,
                            BasicPreferenceProviderImpl(androidContext()),
                            BiometricsConfigApi(d2.httpServiceClient()),
                        )

                    // collect(), not first()/firstOrNull(): those cancel the flow with an
                    // AbortFlowException that sync()'s broad catch logs as a spurious error.
                    runCatching { repository.sync().collect { } }
                },
            )
        }
    }
