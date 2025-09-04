package dev.mskelton.versly

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
    bibleDatabase: BibleDatabase,
    appPreferences: AppPreferences,
) {
    val chapterNum = currentChapter.toIntOrNull() ?: 1
    if (chapterNum < totalChapters) {
        appPreferences.setSelectedChapter((chapterNum + 1).toString())
    } else {
        withContext(Dispatchers.IO) {
            val nextBook = bibleDatabase.getNextBook(currentBook, DEFAULT_TRANSLATION)
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
    bibleDatabase: BibleDatabase,
    appPreferences: AppPreferences,
) {
    val chapterNum = currentChapter.toIntOrNull() ?: 1
    if (chapterNum > 1) {
        appPreferences.setSelectedChapter((chapterNum - 1).toString())
    } else {
        withContext(Dispatchers.IO) {
            val previousBook = bibleDatabase.getPreviousBook(currentBook, DEFAULT_TRANSLATION)
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
    val scrollState = rememberScrollState()

    var books by remember { mutableStateOf<List<BookMetadata>>(emptyList()) }
    var passage by remember { mutableStateOf<Passage?>(null) }

    val selectedBook by appPreferences.selectedBook.collectAsState(initial = "")
    val selectedChapter by appPreferences.selectedChapter.collectAsState(initial = "")
    var totalChapters by remember { mutableIntStateOf(0) }

    val showBookPicker = remember { mutableStateOf(false) }
    val showChapterPicker = remember { mutableStateOf(false) }

    val isLoading = selectedBook.isEmpty() || selectedChapter.isEmpty()

    LaunchedEffect(selectedBook, selectedChapter) {
        if (isLoading) return@LaunchedEffect

        withContext(Dispatchers.IO) {
            books = bibleDatabase.getBookList(DEFAULT_TRANSLATION)
            passage =
                bibleDatabase.getPassage(
                    ChapterId(
                        book = selectedBook,
                        chapter = selectedChapter,
                        translation = DEFAULT_TRANSLATION,
                    )
                )

            val currentBook = bibleDatabase.getBookMetadata(selectedBook, DEFAULT_TRANSLATION)
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
                }
                showBookPicker.value = false
                if (it.chapterCount > 1) {
                    showChapterPicker.value = true
                    totalChapters = it.chapterCount
                }
            },
        )
    } else if (showChapterPicker.value) {
        GridPicker(
            items = (1..totalChapters).map { it.toString() },
            label = { it },
            onItemSelected = {
                scope.launch { appPreferences.setSelectedChapter(it) }
                showChapterPicker.value = false
            },
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.verticalScroll(scrollState).padding(16.dp, 32.dp, 16.dp, 120.dp)
            ) {
                passage?.let { Reader(passage = it) }
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
                            bibleDatabase = bibleDatabase,
                            appPreferences = appPreferences,
                        )
                    }
                },
                onNextChapter = {
                    scope.launch {
                        navigateToNextChapter(
                            currentBook = selectedBook,
                            currentChapter = selectedChapter,
                            totalChapters = totalChapters,
                            bibleDatabase = bibleDatabase,
                            appPreferences = appPreferences,
                        )
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
        modifier = modifier.fillMaxWidth().padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val buttonColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)

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
