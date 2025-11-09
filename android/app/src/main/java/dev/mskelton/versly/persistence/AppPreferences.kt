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
        private val DESTINATION = intPreferencesKey("selected_destination")
        private val BOOK = stringPreferencesKey("selected_book")
        private val CHAPTER = stringPreferencesKey("selected_chapter")
        private val TRANSLATION = stringPreferencesKey("selected_translation")
    }

    val destination: Flow<Int> =
        context.dataStore.data.map { preferences -> preferences[DESTINATION] ?: 0 }

    val passage: Flow<PassageId> =
        context.dataStore.data.map { preferences ->
            val book = preferences[BOOK] ?: "JHN"
            val chapter = preferences[CHAPTER] ?: "1"
            val translation = preferences[TRANSLATION] ?: "ESV"
            PassageId(book, chapter, translation)
        }

    suspend fun setDestination(destination: Int) {
        context.dataStore.edit { preferences -> preferences[DESTINATION] = destination }
    }

    suspend fun setPassage(passageId: PassageId) {
        context.dataStore.edit { preferences ->
            preferences[BOOK] = passageId.book
            preferences[CHAPTER] = passageId.chapter
            preferences[TRANSLATION] = passageId.translation
        }
    }
}

val LocalAppPreferences = compositionLocalOf<AppPreferences> { error("No AppPreferences provided") }
