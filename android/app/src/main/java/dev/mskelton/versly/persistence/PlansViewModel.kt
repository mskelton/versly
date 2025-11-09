package dev.mskelton.versly.persistence

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlansViewModel(bibleDatabase: BibleDatabase, savedStateHandle: SavedStateHandle) :
    PassagesViewModel(bibleDatabase, savedStateHandle, KEY_PASSAGE_IDS) {
    companion object {
        private const val KEY_PASSAGE_IDS = "plans_passage_ids"
    }

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

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
