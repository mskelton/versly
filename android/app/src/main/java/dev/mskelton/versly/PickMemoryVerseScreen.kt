package dev.mskelton.versly

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.mskelton.versly.persistence.BookMetadata
import dev.mskelton.versly.persistence.LocalAppPreferences
import dev.mskelton.versly.persistence.LocalVerslyDatabase
import dev.mskelton.versly.persistence.MemoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.max

private enum class PickStep { BOOK_CHAPTER, VERSE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickMemoryVerseScreen(viewModel: MemoryViewModel) {
    val backStack = LocalBackStack.current
    val db = LocalVerslyDatabase.current
    val appPreferences = LocalAppPreferences.current
    val readViewModel = LocalReadViewModel.current
    val scope = rememberCoroutineScope()

    val translation by appPreferences.translation.collectAsState(initial = "ESV")
    val books by readViewModel.books.collectAsState()

    var step by remember { mutableStateOf(PickStep.BOOK_CHAPTER) }
    var selectedBook by remember { mutableStateOf<BookMetadata?>(null) }
    var selectedChapter by remember { mutableStateOf<String?>(null) }
    var verseCount by remember { mutableIntStateOf(0) }
    var verseStart by remember { mutableStateOf<Int?>(null) }
    var verseEnd by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(selectedBook, selectedChapter, translation) {
        val book = selectedBook?.id ?: return@LaunchedEffect
        val chapter = selectedChapter ?: return@LaunchedEffect
        scope.launch(Dispatchers.IO) {
            verseCount = db.getChapterVerseCount(book, chapter, translation)
        }
    }

    val title = stringResource(R.string.add_memory_verse)

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            when (step) {
                                PickStep.BOOK_CHAPTER -> {
                                    backStack.removeLastOrNull()
                                }

                                PickStep.VERSE -> {
                                    step = PickStep.BOOK_CHAPTER
                                    verseStart = null
                                    verseEnd = null
                                }
                            }
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.chevron_left_24px),
                            contentDescription = stringResource(R.string.navigate_up),
                        )
                    }
                },
                windowInsets = WindowInsets(),
            )
        },
    ) { innerPadding ->
        AnimatedContent(
            targetState = step,
            modifier = Modifier.padding(innerPadding),
        ) { currentStep ->
            when (currentStep) {
                PickStep.BOOK_CHAPTER -> {
                    VerseBookChapterPicker(
                        books = books,
                        onSelect = { book, chapter ->
                            selectedBook = book
                            selectedChapter = chapter
                            verseStart = null
                            verseEnd = null
                            step = PickStep.VERSE
                        },
                    )
                }

                PickStep.VERSE -> {
                    val book = selectedBook ?: return@AnimatedContent
                    val chapter = selectedChapter ?: return@AnimatedContent

                    Column(modifier = Modifier.fillMaxSize()) {
                        val heading =
                            when {
                                verseStart != null && verseEnd != null && verseEnd != verseStart -> {
                                    "${book.title} $chapter:$verseStart–$verseEnd"
                                }

                                verseStart != null -> {
                                    "${book.title} $chapter:$verseStart"
                                }

                                else -> {
                                    "${book.title} $chapter"
                                }
                            }

                        Text(
                            text = heading,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )

                        VerseGrid(
                            verseCount = verseCount,
                            verseStart = verseStart,
                            verseEnd = verseEnd,
                            modifier = Modifier.weight(1f),
                            onVerseClick = { verse ->
                                when {
                                    verseStart == null -> {
                                        verseStart = verse
                                    }

                                    verseEnd == null && verse == verseStart -> {
                                        verseStart = null
                                    }

                                    verseEnd == null -> {
                                        if (verse < verseStart!!) {
                                            verseEnd = verseStart
                                            verseStart = verse
                                        } else {
                                            verseEnd = verse
                                        }
                                    }

                                    else -> {
                                        verseStart = verse
                                        verseEnd = null
                                    }
                                }
                            },
                        )

                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            Button(
                                onClick = {
                                    val start = verseStart ?: return@Button
                                    scope.launch {
                                        viewModel.saveVerse(
                                            book = book.id,
                                            chapter = chapter,
                                            verseStart = start.toString(),
                                            verseEnd = verseEnd?.toString(),
                                            translation = translation,
                                            bookTitle = book.title,
                                        )
                                        backStack.removeLastOrNull()
                                    }
                                },
                                enabled = verseStart != null,
                            ) {
                                Text(stringResource(R.string.save))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VerseBookChapterPicker(
    books: List<BookMetadata>,
    onSelect: (BookMetadata, String) -> Unit,
) {
    val pagerState = rememberPagerState { 2 }
    val coroutineScope = rememberCoroutineScope()

    val oldTestamentBooks = remember(books) { books.take(39) }
    val newTestamentBooks = remember(books) { books.drop(39) }

    Column(modifier = Modifier.fillMaxSize()) {
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
            var selectedBook by remember { mutableStateOf<String?>(null) }

            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                itemsIndexed(filteredBooks, key = { _, book -> book.id }) { _, book ->
                    BookRow(
                        book = book,
                        isExpanded = selectedBook == book.id,
                        onBookClick = { selectedBook = if (selectedBook == book.id) null else book.id },
                        onChapterClick = { chapter -> onSelect(book, chapter.toString()) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VerseGrid(
    verseCount: Int,
    verseStart: Int?,
    verseEnd: Int?,
    onVerseClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rangeStart = minOf(verseStart ?: Int.MAX_VALUE, verseEnd ?: Int.MAX_VALUE)
    val rangeEnd = maxOf(verseStart ?: Int.MIN_VALUE, verseEnd ?: Int.MIN_VALUE)

    BoxWithConstraints(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        val cell = 64.dp
        val columns = max(1, (this.maxWidth / cell).toInt())
        val cellSize = this.maxWidth / columns

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            FlowRow(
                maxItemsInEachRow = columns,
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                for (verse in 1..verseCount) {
                    val isSelected = verse in rangeStart..rangeEnd

                    Box(modifier = Modifier.width(cellSize).wrapContentHeight()) {
                        Surface(
                            modifier = Modifier.padding(4.dp).aspectRatio(1f),
                            color =
                                if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                            tonalElevation = if (isSelected) 0.dp else 2.dp,
                            shape = RoundedCornerShape(8.dp),
                            onClick = { onVerseClick(verse) },
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = verse.toString(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color =
                                        if (isSelected) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
