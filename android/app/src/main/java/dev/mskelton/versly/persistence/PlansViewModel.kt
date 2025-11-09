package dev.mskelton.versly.persistence

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

class PlansViewModel(bibleDatabase: BibleDatabase, savedStateHandle: SavedStateHandle) :
    PassagesViewModel(bibleDatabase, savedStateHandle, KEY_PASSAGE_IDS) {
    companion object {
        private const val KEY_PASSAGE_IDS = "plans_passage_ids"
    }

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val nodes: StateFlow<List<Node>> =
        passages
            .mapLatest { passagesList -> passagesList.flatMap { it.nodes } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
}

class PlansViewModelFactory(private val bibleDatabase: BibleDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val savedStateHandle = extras.createSavedStateHandle()

        @Suppress("UNCHECKED_CAST")
        return PlansViewModel(bibleDatabase, savedStateHandle) as T
    }
}
