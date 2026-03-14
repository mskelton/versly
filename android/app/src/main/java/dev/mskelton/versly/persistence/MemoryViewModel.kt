package dev.mskelton.versly.persistence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mskelton.versly.Memory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = MemoryViewModel.Factory::class)
open class MemoryViewModel
    @AssistedInject
    constructor(
        private val bibleDatabase: BibleDatabase,
        @Assisted val navKey: Memory,
    ) : ViewModel() {
        private val _verses = MutableStateFlow<List<MemoryVerse>>(emptyList())
        val verses: StateFlow<List<MemoryVerse>> = _verses.asStateFlow()

        private val _isLoading = MutableStateFlow(true)
        val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

        init {
            loadVerses()
        }

        private fun loadVerses() {
            viewModelScope.launch(Dispatchers.IO) {
                _verses.value = bibleDatabase.getMemoryVerses()
                _isLoading.value = false
            }
        }

        fun deleteVerse(id: Long) {
            viewModelScope.launch(Dispatchers.IO) {
                bibleDatabase.deleteMemoryVerse(id)
                _verses.value = bibleDatabase.getMemoryVerses()
            }
        }

        fun updateMastery(
            id: Long,
            masteryLevel: Int,
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                bibleDatabase.updateMemoryVerseMastery(id, masteryLevel)
                _verses.value = bibleDatabase.getMemoryVerses()
            }
        }

        fun saveVerse(
            book: String,
            chapter: String,
            verseStart: String,
            verseEnd: String?,
            translation: String,
            bookTitle: String,
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                val text = bibleDatabase.getVerseText(book, chapter, translation, verseStart, verseEnd)
                val reference =
                    if (verseEnd != null && verseEnd != verseStart) {
                        "$bookTitle $chapter:$verseStart–$verseEnd"
                    } else {
                        "$bookTitle $chapter:$verseStart"
                    }
                bibleDatabase.saveMemoryVerse(
                    book = book,
                    chapter = chapter,
                    verseStart = verseStart,
                    verseEnd = verseEnd,
                    translation = translation,
                    text = text,
                    reference = reference,
                )
                _verses.value = bibleDatabase.getMemoryVerses()
            }
        }

        fun getVerseById(id: Long): MemoryVerse? = _verses.value.find { it.id == id }

        @AssistedFactory
        interface Factory {
            fun create(navKey: Memory): MemoryViewModel
        }
    }
