package dev.mskelton.versly

import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

private const val buffer = 3
private const val TAG = "InfiniteLazyColumn"

private fun LazyListState.nearBottom(): Boolean {
    val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull() ?: return false
    return lastVisibleItem.index >= layoutInfo.totalItemsCount - buffer
}

private enum class LoadMore {
    Previous,
    Next,
    None,
}

@Composable
fun <T> InfiniteLazyColumn(
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    loadPrevious: suspend () -> Unit,
    loadNext: suspend () -> Unit,
    content: LazyListScope.() -> Unit,
) {
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { index ->
                if (index <= buffer) return@map LoadMore.Previous
                if (listState.nearBottom()) return@map LoadMore.Next
                return@map LoadMore.None
            }
            .distinctUntilChanged()
            .filter { direction -> direction != LoadMore.None }
            .collect { direction ->
                when (direction) {
                    LoadMore.Previous -> {
                        Log.d(TAG, "Nearing top of list. Loading previous item...")
                        loadPrevious()
                    }
                    LoadMore.Next -> {
                        Log.d(TAG, "Nearing bottom of list. Loading next item...")
                        loadNext()
                    }
                    LoadMore.None -> {}
                }
            }
    }

    LazyColumn(modifier = modifier, state = listState) { content() }
}
