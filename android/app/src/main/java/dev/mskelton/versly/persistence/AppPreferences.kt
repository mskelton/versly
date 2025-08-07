package dev.mskelton.versly.persistence

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "versly_preferences")

class AppPreferences(private val context: Context) {
    companion object {
        private val SELECTED_DESTINATION = intPreferencesKey("selected_destination")
        private val SELECTED_BOOK = stringPreferencesKey("selected_book")
        private val SELECTED_CHAPTER = stringPreferencesKey("selected_chapter")
    }

    val selectedDestination: Flow<Int> =
        context.dataStore.data.map { preferences -> preferences[SELECTED_DESTINATION] ?: 0 }

    val selectedBook: Flow<String> =
        context.dataStore.data.map { preferences -> preferences[SELECTED_BOOK] ?: "JHN" }

    val selectedChapter: Flow<String> =
        context.dataStore.data.map { preferences -> preferences[SELECTED_CHAPTER] ?: "1" }

    suspend fun setSelectedDestination(destination: Int) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_DESTINATION] = destination
        }
    }

    suspend fun setSelectedBook(book: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_BOOK] = book
        }
    }

    suspend fun setSelectedChapter(chapter: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_CHAPTER] = chapter
        }
    }
}

val LocalAppPreferences = compositionLocalOf<AppPreferences> {
    error("No AppPreferences provided")
}
