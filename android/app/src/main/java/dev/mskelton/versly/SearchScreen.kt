package dev.mskelton.versly

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.mskelton.versly.api.LocalVerslyService
import dev.mskelton.versly.persistence.HydratedSearchResult
import dev.mskelton.versly.persistence.LocalAppPreferences
import dev.mskelton.versly.persistence.LocalBibleDatabase
import dev.mskelton.versly.persistence.SearchViewModel

@Composable
fun SearchScreen() {
    val verslyService = LocalVerslyService.current
    val appPreferences = LocalAppPreferences.current
    val bibleDatabase = LocalBibleDatabase.current
    val viewModel: SearchViewModel = viewModel {
        SearchViewModel(appPreferences, verslyService, bibleDatabase)
    }

    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    SearchScreenContent(
        searchQuery = searchQuery,
        onSearchQueryChange = { viewModel.setSearchQuery(it) },
        searchResults = searchResults,
        isLoading = isLoading,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreenContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchResults: List<HydratedSearchResult>,
    isLoading: Boolean,
) {
    val backStack = LocalBackStack.current
    var expanded by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().semantics { isTraversalGroup = true }) {
        SearchBar(
            modifier = Modifier.align(Alignment.TopCenter).semantics { traversalIndex = 0f },
            inputField = {
                SearchBarDefaults.InputField(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onSearch = {
                        // onSearch(textFieldState.text.toString())
                        expanded = false
                    },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    placeholder = { Text(stringResource(R.string.search_the_bible)) },
                    //                 leadingIcon = {
                    //                     Icon(
                    //                         painter = painterResource(R.drawable.search_24px),
                    //                         contentDescription = stringResource(R.string.search),
                    //                     )
                    //                 },
                    trailingIcon = {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(end = 8.dp).size(16.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(
                                painterResource(R.drawable.chevron_left_24px),
                                contentDescription = "Cancel search",
                                Modifier.size(InputChipDefaults.AvatarSize),
                            )
                        }
                    },
                )
            },
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            when {
                searchQuery.isNotBlank() && searchResults.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.no_results_found),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                searchResults.isNotEmpty() -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(searchResults.count()) { index ->
                            val result = searchResults[index]

                            SearchResultItem(
                                result = result,
                                onClick = { backStack.add(Read(result.passageId)) },
                            )
                        }
                    }
                }
            }
        }
    }
}

//         modifier =
//             Modifier.align(Alignment.TopCenter)
//                 .fillMaxWidth()
//                 .padding(horizontal = if (isActive) 0.dp else 16.dp),
//     ) {

@Composable
fun SearchResultItem(result: HydratedSearchResult, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick).fillMaxWidth(),
        headlineContent = { Text(formatPassage(result)) },
        supportingContent = { Text(result.text) },
    )
}

fun formatPassage(result: HydratedSearchResult): String {
    val text = "${result.bookTitle} ${result.passageId.chapter}"
    val start = result.passageId.range?.get(0)
    val end = result.passageId.range?.get(1)

    if (start == null) {
        return text
    }

    if (end != null && start != end) {
        return "$text:$start-$end"
    }

    return "$text:$start"
}
