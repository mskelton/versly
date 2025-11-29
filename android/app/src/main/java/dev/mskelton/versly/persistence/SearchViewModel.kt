package dev.mskelton.versly.persistence

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mskelton.versly.api.VerslyService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

data class HydratedSearchResult(
    val passageId: PassageId,
    val bookTitle: String,
    val text: String,
    val relevance: Float? = null,
)

class SearchViewModel(
    private val appPreferences: AppPreferences,
    private val verslyService: VerslyService,
    private val bibleDatabase: BibleDatabase,
) : ViewModel() {
    var searchQuery = MutableStateFlow("")
        private set

    var isLoading = MutableStateFlow(false)
        private set

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<HydratedSearchResult>> =
        combine(searchQuery.debounce(300), appPreferences.translation) { query, translation ->
                Pair(query, translation)
            }
            .mapLatest { (query, translation) ->
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
            .map {
                withContext(Dispatchers.IO) {
                    it.map { result ->
                        val bookMetadata =
                            bibleDatabase.getBookMetadata(result.book, result.translationId)

                        HydratedSearchResult(
                            passageId =
                                PassageId(
                                    book = result.book,
                                    chapter = result.chapter,
                                    translation = result.translationId,
                                    range = result.range,
                                ),
                            text = "Howdy",
                            bookTitle = bookMetadata?.title ?: result.book,
                            relevance = result.relevance,
                        )
                    }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }
}
