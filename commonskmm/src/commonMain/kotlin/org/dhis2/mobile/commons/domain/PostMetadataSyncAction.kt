package org.dhis2.mobile.commons.domain

/**
 * An extension point for work that must run after a successful metadata sync.
 *
 * The metadata sync use case lives in the `:sync` module, which cannot depend on
 * `:app`. Downstream builds that need to refresh their own configuration when
 * metadata is synced register their actions through DI instead, as a single
 * `List<PostMetadataSyncAction>`:
 *
 * ```kotlin
 * factory<List<PostMetadataSyncAction>> {
 *     listOf(PostMetadataSyncAction { /* refresh some configuration */ })
 * }
 * ```
 *
 * Actions run sequentially, in list order, after the metadata sync itself has
 * succeeded. A failing action is logged and does not fail the metadata sync, so
 * one misbehaving action cannot break syncing for everything else.
 */
fun interface PostMetadataSyncAction {
    suspend operator fun invoke(): Result<Unit>
}
