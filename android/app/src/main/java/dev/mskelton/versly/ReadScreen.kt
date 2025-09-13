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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.mskelton.versly.persistence.BibleDatabase
import dev.mskelton.versly.persistence.BookMetadata
import dev.mskelton.versly.persistence.LocalAppPreferences
import dev.mskelton.versly.persistence.LocalBibleDatabase
import dev.mskelton.versly.persistence.Node
import dev.mskelton.versly.persistence.Passage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val bibleDatabase = LocalBibleDatabase.current
    val appPreferences = LocalAppPreferences.current

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var books by remember { mutableStateOf<List<BookMetadata>>(emptyList()) }

    var passages by rememberSaveable { mutableStateOf<List<Passage>>(emptyList()) }
    val nodes by remember { derivedStateOf { passages.flatMap { it.nodes } } }

    val selectedBook by appPreferences.selectedBook.collectAsState(initial = "")
    val selectedChapter by appPreferences.selectedChapter.collectAsState(initial = "")
    val selectedTranslation by appPreferences.selectedTranslation.collectAsState(initial = "")

    var totalChapters by remember { mutableIntStateOf(0) }
    val showBookPicker = remember { mutableStateOf(false) }
    val showChapterPicker = remember { mutableStateOf(false) }

    val isLoading =
        selectedBook.isEmpty() || selectedChapter.isEmpty() || selectedTranslation.isEmpty()

    LaunchedEffect(isLoading) {
        if (isLoading) return@LaunchedEffect

        withContext(Dispatchers.IO) {
            val currentBook = bibleDatabase.getBookMetadata(selectedBook, selectedTranslation)
            val passage =
                bibleDatabase.getPassage(
                    book = selectedBook,
                    chapter = selectedChapter,
                    translation = selectedTranslation,
                )

            passages = listOf(passage)
            books = bibleDatabase.getBookList(selectedTranslation)
            totalChapters = currentBook?.chapterCount ?: 1
        }
    }

    if (isLoading) {
        LoadingSpinner()
    } else if (showBookPicker.value) {
        BookPicker(
            books = books,
            onBookSelected = {
                scope.launch {
                    appPreferences.setSelectedBook(it.id)
                    appPreferences.setSelectedChapter("1")

                    if (it.chapterCount == 1) {
                        // If the book has only one chapter, scroll to top immediately
                        listState.scrollToItem(0)
                    }
                }
                totalChapters = it.chapterCount
                showChapterPicker.value = it.chapterCount > 1
                showBookPicker.value = false
            },
        )
    } else if (showChapterPicker.value) {
        GridPicker(
            items = (1..totalChapters).map { it.toString() },
            label = { it },
            onItemSelected = {
                scope.launch {
                    appPreferences.setSelectedChapter(it)
                    listState.scrollToItem(0)
                }

                showChapterPicker.value = false
            },
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            nodes.let { nodes ->
                InfiniteLazyColumn<Node>(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    listState = listState,
                    loadPrevious = {
                        val firstPassage = passages.firstOrNull() ?: return@InfiniteLazyColumn
                        val passage = loadPreviousChapter(bibleDatabase, firstPassage)

                        if (passage != null) {
                            passages = listOf(passage) + passages
                        }
                    },
                    loadNext = {
                        val lastPassage = passages.lastOrNull() ?: return@InfiniteLazyColumn
                        val passage = loadNextChapter(bibleDatabase, lastPassage)

                        if (passage != null) {
                            passages = passages + listOf(passage)
                        }
                    },
                    loading = false,
                ) {
                    items(nodes.size, key = { index -> nodes[index].id }) { index ->
                        ReaderNode(nodes[index])
                    }

                    item { Spacer(modifier = Modifier.height(120.dp)) }
                }
            }

            ChapterNavigationFooter(
                selectedBook = selectedBook,
                selectedChapter = selectedChapter,
                books = books,
                onBookChapterClick = { showBookPicker.value = true },
                onPreviousChapter = {},
                onNextChapter = {},
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
    onPreviousChapter: () -> Unit,
    onNextChapter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedBookTitle = books.find { it.id == selectedBook }?.title ?: selectedBook

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.background.copy(alpha = 0.9f))
                .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder(),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val buttonColor = MaterialTheme.colorScheme.secondaryContainer

            IconButton(
                onClick = onPreviousChapter,
                modifier = Modifier.background(buttonColor, RoundedCornerShape(50)),
            ) {
                Icon(
                    painter = painterResource(R.drawable.chevron_left_24px),
                    contentDescription = "Previous Chapter",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }

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
                        .background(buttonColor, RoundedCornerShape(50))
                        .padding(12.dp),
            )

            IconButton(
                onClick = onNextChapter,
                modifier = Modifier.background(buttonColor, RoundedCornerShape(50)),
            ) {
                Icon(
                    painter = painterResource(R.drawable.chevron_right_24px),
                    contentDescription = "Next Chapter",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}
