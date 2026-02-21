@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package dev.mskelton.versly.persistence

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mskelton.versly.Plans
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach

@HiltViewModel(assistedFactory = PlansViewModel.Factory::class)
open class PlansViewModel
    @AssistedInject
    constructor(
        bibleDatabase: BibleDatabase,
        savedStateHandle: SavedStateHandle,
        private val appPreferences: AppPreferences,
        private val planProvider: PlanProvider,
        @Assisted val navKey: Plans,
    ) : PassagesViewModel(bibleDatabase, savedStateHandle, KEY_PASSAGE_IDS) {
        companion object {
            private const val KEY_PASSAGE_IDS = "plans_passage_ids"
        }

        val isLoading = MutableStateFlow(true)

        init {
            appPreferences.translation
                .mapLatest { translation ->
                    planProvider.getReadingsForToday().map { reading ->
                        PassageId(
                            book = reading.book,
                            chapter = reading.chapter,
                            translation = translation,
                            range = reading.range,
                        )
                    }
                }
                .flowOn(Dispatchers.IO)
                .onEach { ids ->
                    setPassageIds(ids)
                    isLoading.value = false
                }
                .launchIn(viewModelScope)
        }

        @AssistedFactory
        interface Factory {
            fun create(navKey: Plans): PlansViewModel
        }
    }
