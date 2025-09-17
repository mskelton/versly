package dev.mskelton.versly

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.mskelton.versly.persistence.BibleDatabase
import dev.mskelton.versly.persistence.BookMetadata
import dev.mskelton.versly.persistence.LocalAppPreferences
import dev.mskelton.versly.persistence.LocalBibleDatabase
import dev.mskelton.versly.persistence.Node
import dev.mskelton.versly.persistence.Passage
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val MAX_PASSAGES = 10

fun loadPreviousChapter(bibleDatabase: BibleDatabase, passage: Passage): Passage? {
    val chapter = passage.chapter.toInt()
    if (chapter > 1) {
        return bibleDatabase.getPassage(
            book = passage.book,
            chapter = (chapter - 1).toString(),
            translation = passage.translation,
        )
    }

    val previousBook = bibleDatabase.getPreviousBook(passage) ?: return null
    return bibleDatabase.getPassage(
        book = previousBook.id,
        chapter = previousBook.chapterCount.toString(),
        translation = passage.translation,
    )
}

fun loadNextChapter(bibleDatabase: BibleDatabase, passage: Passage): Passage? {
    val chapter = passage.chapter.toInt()
    val metadata = bibleDatabase.getBookMetadata(passage.book, passage.translation) ?: return null

    if (chapter < metadata.chapterCount) {
        return bibleDatabase.getPassage(
            book = passage.book,
            chapter = (chapter + 1).toString(),
            translation = passage.translation,
        )
    }

    val nextBook = bibleDatabase.getNextBook(passage) ?: return null
    return bibleDatabase.getPassage(
        book = nextBook.id,
        chapter = "1",
        translation = passage.translation,
    )
}

@Composable
fun ReadScreen() {
    val appPreferences = LocalAppPreferences.current

    val book by appPreferences.selectedBook.collectAsState(initial = "")
    val chapter by appPreferences.selectedChapter.collectAsState(initial = "")
    val translation by appPreferences.selectedTranslation.collectAsState(initial = "")

    val isLoading = book.isEmpty() || chapter.isEmpty() || translation.isEmpty()

    return if (isLoading) {
        LoadingSpinner()
    } else {
        ReadScreenContent(book = book, chapter = chapter, translation = translation)
    }
}

@Composable
fun ReadScreenContent(book: String, chapter: String, translation: String) {
    val bibleDatabase = LocalBibleDatabase.current
    val appPreferences = LocalAppPreferences.current

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var passages by rememberSaveable { mutableStateOf<List<Passage>>(emptyList()) }
    val nodes by remember { derivedStateOf { passages.flatMap { it.nodes } } }
    val mutex by remember { mutableStateOf(Mutex()) }

    var books by remember { mutableStateOf<List<BookMetadata>>(emptyList()) }
    var totalChapters by remember { mutableIntStateOf(0) }
    var showBookPicker by remember { mutableStateOf(false) }
    var showChapterPicker by remember { mutableStateOf(false) }

    LaunchedEffect(translation, showChapterPicker) {
        mutex.withLock {
            val bookList = bibleDatabase.getBookList(translation)
            val passage =
                bibleDatabase.getPassage(book = book, chapter = chapter, translation = translation)

            books = bookList
            passages = listOf(passage)
            listState.scrollToItem(0)
            withFrameNanos { /* Wait for the reader to paint */ }
        }
    }

    if (showBookPicker) {
        BookPicker(
            books = books,
            onBookSelected = {
                totalChapters = it.chapterCount
                showChapterPicker = it.chapterCount > 1
                showBookPicker = false

                scope.launch {
                    appPreferences.setSelectedBook(it.id)
                    appPreferences.setSelectedChapter("1")
                }
            },
        )
    } else if (showChapterPicker) {
        GridPicker(
            items = (1..totalChapters).map { it.toString() },
            label = { it },
            onItemSelected = {
                scope.launch {
                    appPreferences.setSelectedChapter(it)
                    showChapterPicker = false
                }
            },
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            nodes.let { nodes ->
                InfiniteLazyColumn<Node>(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    listState = listState,
                    loadPrevious = {
                        mutex.withLock {
                            val firstPassage = passages.firstOrNull() ?: return@InfiniteLazyColumn
                            val passage = loadPreviousChapter(bibleDatabase, firstPassage)

                            if (passage != null) {
                                passages = (listOf(passage) + passages).take(MAX_PASSAGES)
                            }

                            withFrameNanos { /* Wait for the reader to paint */ }
                        }
                    },
                    loadNext = {
                        mutex.withLock {
                            val lastPassage = passages.lastOrNull() ?: return@InfiniteLazyColumn
                            val passage = loadNextChapter(bibleDatabase, lastPassage)

                            if (passage != null) {
                                passages = (passages + listOf(passage)).takeLast(MAX_PASSAGES)
                            }

                            withFrameNanos { /* Wait for the reader to paint */ }
                        }
                    },
                ) {
                    items(nodes.size, key = { index -> nodes[index].id }) { index ->
                        ReaderNode(nodes[index])
                    }

                    item { Spacer(modifier = Modifier.height(120.dp)) }
                }
            }

            ChapterNavigationFooter(
                selectedBook = book,
                selectedChapter = chapter,
                books = books,
                onBookChapterClick = { showBookPicker = true },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
fun ChapterNavigationFooter(
    selectedBook: String,
    selectedChapter: String,
    books: List<BookMetadata>,
    onBookChapterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedBookTitle = books.find { it.id == selectedBook }?.title ?: selectedBook

    Box(modifier = modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$selectedBookTitle $selectedChapter",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier =
                    Modifier.weight(1f)
                        .padding(horizontal = 16.dp)
                        .clickable { onBookChapterClick() }
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            RoundedCornerShape(50),
                        )
                        .padding(12.dp),
            )
        }
    }
}
