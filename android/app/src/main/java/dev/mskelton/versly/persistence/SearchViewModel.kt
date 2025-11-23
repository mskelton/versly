package dev.mskelton.versly.persistence

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mskelton.versly.api.SearchResult
import dev.mskelton.versly.api.VerslyService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

class SearchViewModel(private val verslyService: VerslyService) : ViewModel() {
    var searchQuery = MutableStateFlow("")
        private set

    var isLoading = MutableStateFlow(false)
        private set

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<SearchResult>> =
        searchQuery
            .debounce(300)
            .mapLatest { query ->
                if (query.isBlank()) {
                    isLoading.value = false
                    return@mapLatest emptyList()
                }

                isLoading.value = true
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
                    isLoading.value = false
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }
}
