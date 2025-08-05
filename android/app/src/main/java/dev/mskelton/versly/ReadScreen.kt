package dev.mskelton.versly

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.mskelton.versly.persistence.BookMetadata
import dev.mskelton.versly.persistence.LocalBibleDatabase
import dev.mskelton.versly.persistence.Passage
import dev.mskelton.versly.persistence.ChapterId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ReadScreen() {
    val bibleDatabase = LocalBibleDatabase.current
    val scrollState = rememberScrollState()
    var books by remember { mutableStateOf<List<BookMetadata>>(emptyList()) }
    var passage by remember { mutableStateOf<Passage?>(null) }
    var selectedBook by remember { mutableStateOf("JHN") }
    var selectedChapter by remember { mutableStateOf("1") }
    var totalChapters by remember { mutableIntStateOf(21) }
    val showBookPicker = remember { mutableStateOf(false) }
    val showChapterPicker = remember { mutableStateOf(false) }
    val chapterId = ChapterId(
        book = selectedBook,
        chapter = selectedChapter,
        translation = DEFAULT_TRANSLATION,
    )

    LaunchedEffect(chapterId) {
        withContext(Dispatchers.IO) {
            books = bibleDatabase.getBookList(DEFAULT_TRANSLATION)
            passage = bibleDatabase.getPassage(chapterId)
        }
    }

    if (showBookPicker.value) {
        GridPicker(
            items = books,
            label = { it.abbreviation },
            onItemSelected = {
                selectedBook = it.id
                selectedChapter = "1"
                totalChapters = it.chapterCount
                showBookPicker.value = false
                showChapterPicker.value = true
            },
        )
    } else if (showChapterPicker.value) {
        GridPicker(
            items = (1..totalChapters).map { it.toString() },
            label = { it },
            onItemSelected = {
                selectedChapter = it
                showChapterPicker.value = false
            },
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(16.dp, 32.dp, 16.dp, 120.dp)
            ) {
                passage?.let {
                    Reader(passage = it)
                }
            }
            
            ChapterNavigationFooter(
                selectedBook = selectedBook,
                selectedChapter = selectedChapter,
                books = books,
                onBookChapterClick = { showBookPicker.value = true },
                onPreviousChapter = { 
                    val chapterNum = selectedChapter.toIntOrNull() ?: 1
                    if (chapterNum > 1) {
                        selectedChapter = (chapterNum - 1).toString()
                    }
                },
                onNextChapter = {
                    val chapterNum = selectedChapter.toIntOrNull() ?: 1
                    if (chapterNum < totalChapters) {
                        selectedChapter = (chapterNum + 1).toString()
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
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
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPreviousChapter,
                modifier = Modifier.background(
                    MaterialTheme.colorScheme.primaryContainer,
                    RoundedCornerShape(8.dp)
                ).padding(4.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.chevron_left_24px),
                    contentDescription = "Previous Chapter",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Text(
                text = "$selectedBookTitle $selectedChapter",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { onBookChapterClick() }
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            )
            
            IconButton(
                onClick = onNextChapter,
                modifier = Modifier.background(
                    MaterialTheme.colorScheme.primaryContainer,
                    RoundedCornerShape(8.dp)
                ).padding(4.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.chevron_right_24px),
                    contentDescription = "Next Chapter",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
