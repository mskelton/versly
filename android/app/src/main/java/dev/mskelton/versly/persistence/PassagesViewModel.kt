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
                decodePassageIds(encodedIds).map { id ->
                    bibleDatabase.getPassage(
                        book = id.book,
                        chapter = id.chapter,
                        translation = id.translation,
                        range = id.range,
                    )
                }
            }.flowOn(Dispatchers.IO)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val nodes: StateFlow<List<Node>> =
        passages
            .mapLatest { passagesList -> passagesList.flatMap { it.nodes } }
            .flowOn(Dispatchers.IO)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    fun setPassageIds(ids: List<PassageId>) {
        savedStateHandle[stateKey] = encodePassageIds(ids)
    }

    private fun encodePassageIds(ids: List<PassageId>): List<String> =
        ids.map { id ->
            val range =
                if (id.range == null) {
                    "*"
                } else {
                    id.range.joinToString("-")
                }

            "${id.book}.${id.chapter}.$range.${id.translation}"
        }

    private fun decodePassageIds(encoded: List<String>): List<PassageId> =
        encoded.map { s ->
            val parts = s.split('.')
            require(parts.size == 4) {
                "Passage id must have 4 parts: book.chapter.range.translation"
            }

            val range = if (parts[2] == "*") null else parts[2].split("-").takeIf { it.size == 2 }

            PassageId(book = parts[0], chapter = parts[1], range = range, translation = parts[3])
        }
}
