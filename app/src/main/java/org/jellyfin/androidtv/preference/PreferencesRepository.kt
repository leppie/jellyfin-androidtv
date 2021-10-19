package org.jellyfin.androidtv.preference

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.jellyfin.sdk.api.client.ApiClient
import kotlin.collections.set

/**
 * Repository to access special preference stores.
 */
class PreferencesRepository(
	private val api: ApiClient,
	private val liveTvPreferences: LiveTvPreferences,
	private val userSettingPreferences: UserSettingPreferences,
) {
	private val libraryPreferences = mutableMapOf<String, LibraryPreferences>()

	fun getLibraryPreferences(preferencesId: String): LibraryPreferences {
		val store = libraryPreferences[preferencesId] ?: LibraryPreferences(preferencesId, api)

		libraryPreferences[preferencesId] = store

		// FIXME: Make [getLibraryPreferences] suspended when usages are converted to Kotlin
		if (store.shouldUpdate) store.updateBlocking()

		return store
	}

	suspend fun onSessionChanged() = coroutineScope {
		awaitAll(
			async { liveTvPreferences.update() },
			async { userSettingPreferences.update() },
		)

		libraryPreferences.clear()
	}
}