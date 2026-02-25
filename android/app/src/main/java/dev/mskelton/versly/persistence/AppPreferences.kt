package dev.mskelton.versly.persistence

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "versly_preferences")

open class AppPreferences(
    private val context: Context,
) {
    companion object {
        private val BOOK = stringPreferencesKey("selected_book")
        private val CHAPTER = stringPreferencesKey("selected_chapter")
        private val TRANSLATION = stringPreferencesKey("selected_translation")
        private val TAB = stringPreferencesKey("selected_tab")
    }

    open val translation: Flow<String> =
        context.dataStore.data.map { preferences -> preferences[TRANSLATION] ?: "ESV" }

    open suspend fun setTranslation(translation: String) {
        context.dataStore.edit { preferences -> preferences[TRANSLATION] = translation }
    }

    open val passage: Flow<PassageId> =
        context.dataStore.data.map { preferences ->
            val book = preferences[BOOK] ?: "JHN"
            val chapter = preferences[CHAPTER] ?: "3"
            val translation = preferences[TRANSLATION] ?: "ESV"
            PassageId(book, chapter, translation)
        }

    open suspend fun setPassage(passageId: PassageId) {
        context.dataStore.edit { preferences ->
            preferences[BOOK] = passageId.book
            preferences[CHAPTER] = passageId.chapter
            preferences[TRANSLATION] = passageId.translation
        }
    }

    open val lastTab: Flow<String> =
        context.dataStore.data.map { preferences -> preferences[TAB] ?: "read" }

    open suspend fun setLastTab(tab: String) {
        context.dataStore.edit { preferences -> preferences[TAB] = tab }
    }
}

val LocalAppPreferences = compositionLocalOf<AppPreferences> { error("No AppPreferences provided") }
