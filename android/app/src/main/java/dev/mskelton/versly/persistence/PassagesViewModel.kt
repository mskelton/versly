package dev.mskelton.versly.persistence

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

abstract class PassagesViewModel(
    protected val bibleDatabase: BibleDatabase,
    protected val savedStateHandle: SavedStateHandle,
    private val stateKey: String,
) : ViewModel() {
    private val passageIdStrings = savedStateHandle.getStateFlow(stateKey, emptyList<String>())

    @OptIn(ExperimentalCoroutinesApi::class)
    val passageIds: StateFlow<List<PassageId>> =
        passageIdStrings
            .mapLatest { encodedIds -> decodePassageIds(encodedIds) }
            .flowOn(Dispatchers.IO)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val passages: StateFlow<List<Passage>> =
        passageIdStrings
            .mapLatest { encodedIds ->
                decodePassageIds(encodedIds).mapNotNull { id ->
                    try {
                        bibleDatabase.getPassage(
                            book = id.book,
                            chapter = id.chapter,
                            translation = id.translation,
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            .flowOn(Dispatchers.IO)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    fun setPassageIds(ids: List<PassageId>) {
        savedStateHandle[stateKey] = encodePassageIds(ids)
    }

    private fun encodePassageIds(ids: List<PassageId>): List<String> {
        return ids.map { "${it.book}.${it.chapter}.${it.translation}" }
    }

    private fun decodePassageIds(encoded: List<String>): List<PassageId> {
        return encoded.map { s ->
            val (book, chapter, translation) = s.split('.', limit = 3)
            PassageId(book, chapter, translation)
        }
    }
}
