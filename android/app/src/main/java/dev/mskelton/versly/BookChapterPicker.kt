package dev.mskelton.versly

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.mskelton.versly.persistence.BookMetadata
import dev.mskelton.versly.persistence.PassageId
import dev.mskelton.versly.ui.theme.VerslyTheme
import kotlinx.coroutines.launch
import kotlin.math.max

enum class Testament {
    OLD,
    NEW,
}

@Composable
fun BookChapterPicker(
    passageId: PassageId,
    onSelect: (book: String, chapter: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val readViewModel = LocalReadViewModel.current
    val books by readViewModel.books.collectAsState()

    val currentBookIndex = remember(books, passageId) { books.indexOfFirst { it.id == passageId.book } }
    val initialTestament = if (currentBookIndex >= 39) Testament.NEW.ordinal else Testament.OLD.ordinal

    val pagerState = rememberPagerState(initialPage = initialTestament) { 2 }
    val coroutineScope = rememberCoroutineScope()

    val oldTestamentBooks = remember(books) { books.take(39) }
    val newTestamentBooks = remember(books) { books.drop(39) }

    Column(modifier = modifier.fillMaxSize()) {
        SecondaryTabRow(selectedTabIndex = pagerState.currentPage) {
            Tab(
                selected = pagerState.currentPage == Testament.OLD.ordinal,
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(Testament.OLD.ordinal) } },
                text = { Text(stringResource(R.string.old_testament)) },
            )
            Tab(
                selected = pagerState.currentPage == Testament.NEW.ordinal,
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(Testament.NEW.ordinal) } },
                text = { Text(stringResource(R.string.new_testament)) },
            )
        }

        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val filteredBooks = if (page == Testament.OLD.ordinal) oldTestamentBooks else newTestamentBooks
            BookList(
                books = filteredBooks,
                passageId = passageId,
                onSelect = onSelect,
            )
        }
    }
}

@Composable
fun BookList(
    books: List<BookMetadata>,
    passageId: PassageId,
    onSelect: (book: String, chapter: String) -> Unit,
) {
    var selectedBook by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(books) {
        val index = books.indexOfFirst { it.id == passageId.book }
        if (index >= 0) {
            listState.scrollToItem(index)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        itemsIndexed(books, key = { _, book -> book.id }) { index, book ->
            BookRow(
                book = book,
                isExpanded = selectedBook == book.id,
                onBookClick = {
                    val wasExpanded = selectedBook == book.id
                    selectedBook = if (wasExpanded) null else book.id
                    if (!wasExpanded) {
                        coroutineScope.launch { listState.animateScrollToItem(index) }
                    }
                },
                onChapterClick = { chapter -> onSelect(book.id, chapter.toString()) },
            )
        }
    }
}

@Composable
fun BookRow(
    book: BookMetadata,
    isExpanded: Boolean,
    onBookClick: () -> Unit,
    onChapterClick: (Int) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            shape = RoundedCornerShape(8.dp),
            onClick = onBookClick,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            ChapterGrid(count = book.chapterCount, onChapterClick = onChapterClick)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChapterGrid(
    count: Int,
    onChapterClick: (Int) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        val cell = 64.dp
        val columns = max(1, (this.maxWidth / cell).toInt())
        val cellSize = this.maxWidth / columns

        FlowRow(
            maxItemsInEachRow = columns,
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            repeat(count) { item ->
                Box(modifier = Modifier.width(cellSize).wrapContentHeight()) {
                    val chapter = item + 1

                    Surface(
                        modifier = Modifier.padding(4.dp).aspectRatio(1f),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        shape = RoundedCornerShape(8.dp),
                        onClick = { onChapterClick(chapter) },
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = chapter.toString(),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookChapterPickerScreen(passageId: PassageId) {
    val backStack = LocalBackStack.current
    val readViewModel = LocalReadViewModel.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Column {
        TopAppBar(
            title = { Text(stringResource(R.string.select_passage)) },
            navigationIcon = {
                IconButton(onClick = { backStack.removeLastOrNull() }) {
                    Icon(
                        painter = painterResource(R.drawable.chevron_left_24px),
                        contentDescription = stringResource(R.string.navigate_up),
                    )
                }
            },
            scrollBehavior = scrollBehavior,
            windowInsets = WindowInsets(),
        )

        BookChapterPicker(
            passageId = passageId,
            onSelect = { book, chapter ->
                readViewModel.setCurrentPassage(passageId.copy(book = book, chapter = chapter))
                backStack.removeLastOrNull()
            },
        )
    }
}

@Preview
@Composable
fun BookRowPreview() {
    val book = BookMetadata(id = "GEN", title = "Genesis", abbreviation = "Gen.", chapterCount = 50)
    VerslyTheme { BookRow(book = book, isExpanded = false, onBookClick = {}, onChapterClick = {}) }
}

@Preview
@Composable
fun ChapterGridPreview() {
    VerslyTheme { ChapterGrid(count = 50, onChapterClick = {}) }
}
