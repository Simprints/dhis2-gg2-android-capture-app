package org.dhis2.di

import org.koin.dsl.module

/**
 * This flavor registers no post-metadata-sync actions.
 *
 * The file must exist in every flavor source set so that `KoinInitialization` can
 * register the module unconditionally. A flavor that needs work to run after a
 * metadata sync declares its own `List<PostMetadataSyncAction>` here.
 */
val postMetadataSyncModule = module { }
