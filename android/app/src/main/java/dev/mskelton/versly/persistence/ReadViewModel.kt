package dev.mskelton.versly.persistence

import androidx.lifecycle.SavedStateHandle
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mskelton.versly.Read

@HiltViewModel(assistedFactory = ReadViewModel.Factory::class)
class ReadViewModel
    @AssistedInject
    constructor(
        bibleDatabase: BibleDatabase,
        savedStateHandle: SavedStateHandle,
        @Assisted val navKey: Read,
    ) : PassagesViewModel(bibleDatabase, savedStateHandle, KEY_PASSAGE_IDS) {
        companion object {
            private const val KEY_PASSAGE_IDS = "read_passage_ids"
        }

        @AssistedFactory
        interface Factory {
            fun create(navKey: Read): ReadViewModel
        }
    }
