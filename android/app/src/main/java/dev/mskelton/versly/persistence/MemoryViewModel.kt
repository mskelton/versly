package dev.mskelton.versly.persistence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class MemoryViewModel
    @Inject
    constructor(
        private val db: VerslyDatabase,
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
                _verses.value = db.getMemoryVerses()
                _isLoading.value = false
            }
        }

        fun deleteVerse(id: Long) {
            viewModelScope.launch(Dispatchers.IO) {
                db.deleteMemoryVerse(id)
                _verses.value = db.getMemoryVerses()
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
                val text = db.getVerseText(book, chapter, translation, verseStart, verseEnd)
                val reference =
                    if (verseEnd != null && verseEnd != verseStart) {
                        "$bookTitle $chapter:$verseStart–$verseEnd"
                    } else {
                        "$bookTitle $chapter:$verseStart"
                    }
                db.saveMemoryVerse(
                    book = book,
                    chapter = chapter,
                    verseStart = verseStart,
                    verseEnd = verseEnd,
                    translation = translation,
                    text = text,
                    reference = reference,
                )
                _verses.value = db.getMemoryVerses()
            }
        }

        fun getVerseById(id: Long): MemoryVerse? = _verses.value.find { it.id == id }
    }
