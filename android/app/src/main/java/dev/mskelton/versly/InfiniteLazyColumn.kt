package dev.mskelton.versly

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

private const val buffer = 10

internal fun LazyListState.nearTop(): Boolean {
    val firstVisibleItem = this.layoutInfo.visibleItemsInfo.firstOrNull()
    return firstVisibleItem != null && firstVisibleItem.index <= buffer
}

internal fun LazyListState.nearBottom(): Boolean {
    val lastVisibleItem = this.layoutInfo.visibleItemsInfo.lastOrNull()
    return lastVisibleItem != null &&
        lastVisibleItem.index >= (this.layoutInfo.totalItemsCount - 1 - buffer)
}

@Composable
fun <T> InfiniteLazyColumn(
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    listState: LazyListState = rememberLazyListState(),
    loadPrevious: () -> Unit,
    loadNext: () -> Unit,
    content: LazyListScope.() -> Unit,
) {
    val nearTop by remember { derivedStateOf { listState.nearTop() } }
    val nearBottom by remember { derivedStateOf { listState.nearBottom() } }

    LaunchedEffect(nearTop) { if (nearTop && !loading) loadPrevious() }
    LaunchedEffect(nearBottom) { if (nearBottom && !loading) loadNext() }

    LazyColumn(modifier = modifier, state = listState) { content() }
}
