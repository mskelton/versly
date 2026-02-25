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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ReadViewModel.Factory::class)
open class ReadViewModel
    @AssistedInject
    constructor(
        private val bibleDatabase: BibleDatabase,
        private val savedStateHandle: SavedStateHandle,
        @Assisted val navKey: Read,
    ) : ViewModel() {
        companion object {
            private const val KEY_CURRENT_PASSAGE = "current_passage_id"
        }

        private val _slideDirection = MutableStateFlow(1)
        val slideDirection: StateFlow<Int> = _slideDirection.asStateFlow()

        private val _currentPassageId =
            MutableStateFlow(
                savedStateHandle.get<String>(KEY_CURRENT_PASSAGE)?.let { decodePassageId(it) }
                    ?: navKey.passageId,
            )
        val currentPassageId: StateFlow<PassageId?> = _currentPassageId.asStateFlow()

        fun setCurrentPassage(id: PassageId) {
            savedStateHandle[KEY_CURRENT_PASSAGE] = encodePassageId(id)
            _currentPassageId.value = id
        }

        fun navigateNext() {
            val current = _currentPassageId.value ?: return
            viewModelScope.launch(Dispatchers.IO) {
                val passage =
                    bibleDatabase.getPassage(
                        book = current.book,
                        chapter = current.chapter,
                        translation = current.translation,
                        range = current.range,
                    )
                val next = loadNextChapter(passage) ?: return@launch
                _slideDirection.value = 1
                setCurrentPassage(next.id)
            }
        }

        fun navigatePrevious() {
            val current = _currentPassageId.value ?: return
            viewModelScope.launch(Dispatchers.IO) {
                val passage =
                    bibleDatabase.getPassage(
                        book = current.book,
                        chapter = current.chapter,
                        translation = current.translation,
                        range = current.range,
                    )
                val prev = loadPreviousChapter(passage) ?: return@launch
                _slideDirection.value = -1
                setCurrentPassage(prev.id)
            }
        }

        private fun loadNextChapter(passage: Passage): Passage? {
            val chapter = passage.chapter.toInt()
            val metadata = bibleDatabase.getBookMetadata(passage.book, passage.translation) ?: return null

            if (chapter < metadata.chapterCount) {
                return bibleDatabase.getPassage(
                    book = passage.book,
                    chapter = (chapter + 1).toString(),
                    translation = passage.translation,
                )
            }

            val nextBook = bibleDatabase.getNextBook(passage) ?: return null
            return bibleDatabase.getPassage(book = nextBook.id, chapter = "1", translation = passage.translation)
        }

        private fun loadPreviousChapter(passage: Passage): Passage? {
            val chapter = passage.chapter.toInt()
            if (chapter > 1) {
                return bibleDatabase.getPassage(
                    book = passage.book,
                    chapter = (chapter - 1).toString(),
                    translation = passage.translation,
                )
            }

            val previousBook = bibleDatabase.getPreviousBook(passage) ?: return null
            return bibleDatabase.getPassage(
                book = previousBook.id,
                chapter = previousBook.chapterCount.toString(),
                translation = passage.translation,
            )
        }

        fun getBookTitle(passageId: PassageId, nodes: List<Node>): String {
            val chapterNode = nodes.firstOrNull() ?: return passageId.book
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
