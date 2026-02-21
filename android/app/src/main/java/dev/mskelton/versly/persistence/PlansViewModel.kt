package dev.mskelton.versly.persistence

import androidx.lifecycle.SavedStateHandle
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mskelton.versly.Plans
import kotlinx.coroutines.flow.MutableStateFlow

@HiltViewModel(assistedFactory = PlansViewModel.Factory::class)
open class PlansViewModel
    @AssistedInject
    constructor(
        bibleDatabase: BibleDatabase,
        savedStateHandle: SavedStateHandle,
        @Assisted val navKey: Plans,
    ) : PassagesViewModel(bibleDatabase, savedStateHandle, KEY_PASSAGE_IDS) {
        companion object {
            private const val KEY_PASSAGE_IDS = "plans_passage_ids"
        }

        var isLoading = MutableStateFlow(true)
            private set

        fun setLoading(loading: Boolean) {
            isLoading.value = loading
        }

        @AssistedFactory
        interface Factory {
            fun create(navKey: Plans): PlansViewModel
        }
    }
