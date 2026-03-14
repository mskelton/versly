@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package dev.mskelton.versly.persistence

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
open class PlansViewModel
    @Inject
    constructor(
        private val db: VerslyDatabase,
        private val savedStateHandle: SavedStateHandle,
        appPreferences: AppPreferences,
        private val planProvider: PlanProvider,
    ) : ViewModel() {
        companion object {
            private const val KEY_PASSAGE_IDS = "plans_passage_ids"
        }

        private val passageIdStrings =
            savedStateHandle.getStateFlow(KEY_PASSAGE_IDS, emptyList<String>())

        val passages: StateFlow<List<Passage>> =
            passageIdStrings
                .mapLatest { encodedIds ->
                    encodedIds.mapNotNull { decodePassageId(it) }.map { id ->
                        db.getPassage(
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

        val nodes: StateFlow<List<Node>> =
            passages
                .mapLatest { passagesList -> passagesList.flatMap { it.nodes } }
                .flowOn(Dispatchers.IO)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList(),
                )

        val isLoading = MutableStateFlow(true)
        val dayNumber = MutableStateFlow<Int?>(null)

        init {
            appPreferences.translation
                .mapLatest { translation ->
                    val plan = planProvider.getCurrentPlanDay()
                    val ids =
                        plan?.readings?.map { reading ->
                            PassageId(
                                book = reading.book,
                                chapter = reading.chapter,
                                translation = translation,
                                range = reading.range,
                            )
                        } ?: emptyList()
                    Pair(plan?.dayNumber, ids)
                }.flowOn(Dispatchers.IO)
                .onEach { (number, ids) ->
                    dayNumber.value = number
                    setPassageIds(ids)
                    isLoading.value = false
                }.launchIn(viewModelScope)
        }

        fun setPassageIds(ids: List<PassageId>) {
            savedStateHandle[KEY_PASSAGE_IDS] = ids.map { encodePassageId(it) }
        }

    }
