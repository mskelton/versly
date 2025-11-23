package dev.mskelton.versly.persistence

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mskelton.versly.api.SearchResult
import dev.mskelton.versly.api.VerslyService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

class SearchViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val verslyService: VerslyService,
) : ViewModel() {
    val searchQuery = savedStateHandle.getStateFlow("searchQuery", "")

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<SearchResult>> =
        searchQuery
            .debounce(300)
            .mapLatest { query ->
                if (query.isBlank()) {
                    _isLoading.value = false
                    return@mapLatest emptyList()
                }

                _isLoading.value = true
                try {
                    val response = verslyService.search(query)
                    if (response.isSuccessful) {
                        response.body()?.results ?: emptyList()
                    } else {
                        Log.e("SearchViewModel", "Search failed: ${response.errorBody()?.string()}")
                        emptyList()
                    }
                } catch (e: Exception) {
                    Log.e("SearchViewModel", "Search error", e)
                    emptyList()
                } finally {
                    _isLoading.value = false
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        savedStateHandle["searchQuery"] = query
    }
}
