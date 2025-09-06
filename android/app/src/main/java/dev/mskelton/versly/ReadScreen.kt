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
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.mskelton.versly.persistence.AppPreferences
import dev.mskelton.versly.persistence.BibleDatabase
import dev.mskelton.versly.persistence.BookMetadata
import dev.mskelton.versly.persistence.ChapterId
import dev.mskelton.versly.persistence.LocalAppPreferences
import dev.mskelton.versly.persistence.LocalBibleDatabase
import dev.mskelton.versly.persistence.Passage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

suspend fun navigateToNextChapter(
    currentBook: String,
    currentChapter: String,
    totalChapters: Int,
    selectedTranslation: String,
    bibleDatabase: BibleDatabase,
    appPreferences: AppPreferences,
) {
    val chapterNum = currentChapter.toIntOrNull() ?: 1
    if (chapterNum < totalChapters) {
        appPreferences.setSelectedChapter((chapterNum + 1).toString())
    } else {
        withContext(Dispatchers.IO) {
            val nextBook = bibleDatabase.getNextBook(currentBook, selectedTranslation)
            nextBook?.let { book ->
                appPreferences.setSelectedBook(book.id)
                appPreferences.setSelectedChapter("1")
            }
        }
    }
}

suspend fun navigateToPreviousChapter(
    currentBook: String,
    currentChapter: String,
    selectedTranslation: String,
    bibleDatabase: BibleDatabase,
    appPreferences: AppPreferences,
) {
    val chapterNum = currentChapter.toIntOrNull() ?: 1
    if (chapterNum > 1) {
        appPreferences.setSelectedChapter((chapterNum - 1).toString())
    } else {
        withContext(Dispatchers.IO) {
            val previousBook = bibleDatabase.getPreviousBook(currentBook, selectedTranslation)
            previousBook?.let { book ->
                appPreferences.setSelectedBook(book.id)
                appPreferences.setSelectedChapter(book.chapterCount.toString())
            }
        }
    }
}

@Composable
fun ReadScreen() {
    val bibleDatabase = LocalBibleDatabase.current
    val appPreferences = LocalAppPreferences.current

    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    var books by remember { mutableStateOf<List<BookMetadata>>(emptyList()) }
    var passage by remember { mutableStateOf<Passage?>(null) }

    val selectedBook by appPreferences.selectedBook.collectAsState(initial = "")
    val selectedChapter by appPreferences.selectedChapter.collectAsState(initial = "")
    val selectedTranslation by appPreferences.selectedTranslation.collectAsState(initial = "")
    var totalChapters by remember { mutableIntStateOf(0) }

    val showBookPicker = remember { mutableStateOf(false) }
    val showChapterPicker = remember { mutableStateOf(false) }

    val isLoading =
        selectedBook.isEmpty() || selectedChapter.isEmpty() || selectedTranslation.isEmpty()

    LaunchedEffect(selectedBook, selectedChapter, selectedTranslation) {
        if (isLoading) return@LaunchedEffect

        withContext(Dispatchers.IO) {
            books = bibleDatabase.getBookList(selectedTranslation)
            passage =
                bibleDatabase.getPassage(
                    ChapterId(
                        book = selectedBook,
                        chapter = selectedChapter,
                        translation = selectedTranslation,
                    )
                )

            val currentBook = bibleDatabase.getBookMetadata(selectedBook, selectedTranslation)
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
                        lazyListState.scrollToItem(0)
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
                    lazyListState.scrollToItem(0)
                }

                showChapterPicker.value = false
            },
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            passage?.let {
                LazyColumn(modifier = Modifier.padding(horizontal = 16.dp), state = lazyListState) {
                    items(
                        it.nodes.length(),
                        key = { index -> "${it.id.chapter}.${it.id.book}.$index" },
                    ) { index ->
                        ReaderNode(it.nodes.getJSONArray(index))
                    }

                    item { Spacer(modifier = Modifier.height(120.dp)) }
                }
            }

            ChapterNavigationFooter(
                selectedBook = selectedBook,
                selectedChapter = selectedChapter,
                books = books,
                onBookChapterClick = { showBookPicker.value = true },
                onPreviousChapter = {
                    scope.launch {
                        navigateToPreviousChapter(
                            currentBook = selectedBook,
                            currentChapter = selectedChapter,
                            selectedTranslation = selectedTranslation,
                            bibleDatabase = bibleDatabase,
                            appPreferences = appPreferences,
                        )
                        lazyListState.scrollToItem(0)
                    }
                },
                onNextChapter = {
                    scope.launch {
                        navigateToNextChapter(
                            currentBook = selectedBook,
                            currentChapter = selectedChapter,
                            totalChapters = totalChapters,
                            selectedTranslation = selectedTranslation,
                            bibleDatabase = bibleDatabase,
                            appPreferences = appPreferences,
                        )
                        lazyListState.scrollToItem(0)
                    }
                },
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
