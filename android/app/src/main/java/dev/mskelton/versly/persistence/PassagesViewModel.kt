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
            .mapLatest { encodedIds -> encodedIds.mapNotNull { decodePassageId(it) } }
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
                encodedIds.mapNotNull { decodePassageId(it) }.map { id ->
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
        savedStateHandle[stateKey] = ids.map { encodePassageId(it) }
    }
}
