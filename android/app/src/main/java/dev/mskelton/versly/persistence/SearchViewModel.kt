package dev.mskelton.versly.persistence

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mskelton.versly.Search
import dev.mskelton.versly.api.VerslyService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

data class HydratedSearchResult(val passageId: PassageId, val bookTitle: String, val text: String)

@HiltViewModel(assistedFactory = SearchViewModel.Factory::class)
class SearchViewModel
@AssistedInject
constructor(
    appPreferences: AppPreferences,
    verslyService: VerslyService,
    bibleDatabase: BibleDatabase,
    @Assisted val navKey: Search,
) : ViewModel() {
    var searchQuery = MutableStateFlow("")
        private set

    var isLoading = MutableStateFlow(false)
        private set

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<HydratedSearchResult>> =
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
            .combine(appPreferences.translation) { results, translation ->
                Pair(results, translation)
            }
            .mapLatest { (results, translation) ->
                withContext(Dispatchers.IO) {
                    val passageIds =
                        results.map {
                            PassageId(
                                book = it.book,
                                chapter = it.chapter,
                                range = it.range,
                                translation = translation,
                            )
                        }

                    bibleDatabase.bulkGetPassages(passageIds).map {
                        HydratedSearchResult(
                            passageId = it.id,
                            text = it.text,
                            bookTitle = it.bookTitle,
                        )
                    }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    @AssistedFactory
    interface Factory {
        fun create(navKey: Search): SearchViewModel
    }
}
