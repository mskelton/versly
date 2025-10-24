package dev.mskelton.versly.persistence

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras

class ReadViewModel(bibleDatabase: BibleDatabase, savedStateHandle: SavedStateHandle) :
    PassagesViewModel(bibleDatabase, savedStateHandle, KEY_PASSAGE_IDS) {
    companion object {
        private const val KEY_PASSAGE_IDS = "read_passage_ids"
    }
}

class ReadViewModelFactory(private val bibleDatabase: BibleDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val savedStateHandle = extras.createSavedStateHandle()

        @Suppress("UNCHECKED_CAST")
        return ReadViewModel(bibleDatabase, savedStateHandle) as T
    }
}
