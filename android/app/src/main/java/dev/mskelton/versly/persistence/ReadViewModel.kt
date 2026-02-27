package dev.mskelton.versly.persistence

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mskelton.versly.Read
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ReadViewModel.Factory::class)
open class ReadViewModel
    @AssistedInject
    constructor(
        private val bibleDatabase: BibleDatabase,
        private val savedStateHandle: SavedStateHandle,
        private val appPreferences: AppPreferences,
        @Assisted val navKey: Read,
    ) : ViewModel() {
        companion object {
            private const val KEY_CURRENT_PASSAGE = "current_passage_id"
        }

        private val _slideDirection = MutableStateFlow(1)
        val slideDirection: StateFlow<Int> = _slideDirection.asStateFlow()

        @OptIn(ExperimentalCoroutinesApi::class)
        val currentPassageId: StateFlow<PassageId?> =
            savedStateHandle
                .getStateFlow<String?>(KEY_CURRENT_PASSAGE, null)
                .flatMapLatest { saved ->
                    if (saved != null) flowOf(decodePassageId(saved)) else appPreferences.passage
                }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

        @OptIn(ExperimentalCoroutinesApi::class)
        val books: StateFlow<List<BookMetadata>> =
            currentPassageId
                .mapLatest { it?.translation }
                .distinctUntilChanged()
                .flatMapLatest { translation ->
                    if (translation != null) {
                        flow { emit(bibleDatabase.getBookList(translation)) }.flowOn(Dispatchers.IO)
                    } else {
                        flowOf(emptyList())
                    }
                }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

        fun setCurrentPassage(id: PassageId) {
            savedStateHandle[KEY_CURRENT_PASSAGE] = encodePassageId(id)
            viewModelScope.launch { appPreferences.setPassage(id) }
        }

        fun navigateNext() {
            val current = currentPassageId.value ?: return
            viewModelScope.launch(Dispatchers.IO) {
                val next = loadNextChapter(current) ?: return@launch
                _slideDirection.value = 1
                setCurrentPassage(next.id)
            }
        }

        fun navigatePrevious() {
            val current = currentPassageId.value ?: return
            viewModelScope.launch(Dispatchers.IO) {
                val prev = loadPreviousChapter(current) ?: return@launch
                _slideDirection.value = -1
                setCurrentPassage(prev.id)
            }
        }

        private fun loadNextChapter(passageId: PassageId): Passage? {
            val chapter = passageId.chapter.toInt()
            val metadata = bibleDatabase.getBookMetadata(passageId.book, passageId.translation) ?: return null

            if (chapter < metadata.chapterCount) {
                return bibleDatabase.getPassage(
                    book = passageId.book,
                    chapter = (chapter + 1).toString(),
                    translation = passageId.translation,
                )
            }

            val nextBook = bibleDatabase.getNextBook(passageId) ?: return null
            return bibleDatabase.getPassage(book = nextBook.id, chapter = "1", translation = passageId.translation)
        }

        private fun loadPreviousChapter(passageId: PassageId): Passage? {
            val chapter = passageId.chapter.toInt()
            if (chapter > 1) {
                return bibleDatabase.getPassage(
                    book = passageId.book,
                    chapter = (chapter - 1).toString(),
                    translation = passageId.translation,
                )
            }

            val previousBook = bibleDatabase.getPreviousBook(passageId) ?: return null
            return bibleDatabase.getPassage(
                book = previousBook.id,
                chapter = previousBook.chapterCount.toString(),
                translation = passageId.translation,
            )
        }

        fun getBookTitle(
            passageId: PassageId,
            nodes: List<Node>,
        ): String? {
            if (nodes.isEmpty()) return null
            val chapterNode = nodes.first()
            return if (chapterNode.data.getString(0) == "zc") chapterNode.data.getString(1) else passageId.book
        }

        private val nodeFlowCache = mutableMapOf<PassageId, StateFlow<List<Node>>>()

        @OptIn(ExperimentalCoroutinesApi::class)
        fun nodesFor(passageId: PassageId): StateFlow<List<Node>> =
            nodeFlowCache.getOrPut(passageId) {
                flow {
                    emit(
                        bibleDatabase
                            .getPassage(
                                book = passageId.book,
                                chapter = passageId.chapter,
                                translation = passageId.translation,
                                range = passageId.range,
                            ).nodes,
                    )
                }.flowOn(Dispatchers.IO)
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = emptyList(),
                    )
            }

        @AssistedFactory
        interface Factory {
            fun create(navKey: Read): ReadViewModel
        }
    }
