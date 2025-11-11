package dev.mskelton.versly

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.mskelton.versly.persistence.BookMetadata
import dev.mskelton.versly.persistence.LocalAppPreferences
import dev.mskelton.versly.persistence.LocalBibleDatabase
import dev.mskelton.versly.persistence.PassageId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Testament {
    OLD,
    NEW,
}

@Composable
fun BookChapterPicker(passageId: PassageId, onSelect: () -> Unit) {
    val bibleDatabase = LocalBibleDatabase.current
    val appPreferences = LocalAppPreferences.current
    val scope = rememberCoroutineScope()

    var books by remember { mutableStateOf<List<BookMetadata>>(emptyList()) }
    var selectedTestament by remember { mutableIntStateOf(Testament.OLD.ordinal) }
    var expandedBookId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(passageId.translation) {
        withContext(Dispatchers.IO) { books = bibleDatabase.getBookList(passageId.translation) }
    }

    val oldTestamentBooks = books.take(39)
    val newTestamentBooks = books.drop(39)
    val displayedBooks =
        if (selectedTestament == Testament.OLD.ordinal) oldTestamentBooks else newTestamentBooks

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTestament) {
            Tab(
                selected = selectedTestament == Testament.OLD.ordinal,
                onClick = { selectedTestament = Testament.OLD.ordinal },
                text = { Text("Old Testament") },
            )
            Tab(
                selected = selectedTestament == Testament.NEW.ordinal,
                onClick = { selectedTestament = Testament.NEW.ordinal },
                text = { Text("New Testament") },
            )
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            displayedBooks.forEach { book ->
                BookRow(
                    book = book,
                    isExpanded = expandedBookId == book.id,
                    onBookClick = {
                        expandedBookId = if (expandedBookId == book.id) null else book.id
                    },
                    onChapterClick = { chapter ->
                        scope.launch {
                            appPreferences.setPassage(
                                passageId.copy(book = book.id, chapter = chapter.toString())
                            )
                            onSelect()
                        }
                    },
                )
            }
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
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onBookClick() },
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "${book.chapterCount} chapters",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (isExpanded) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(64.dp),
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                userScrollEnabled = false,
            ) {
                items(book.chapterCount) { index ->
                    val chapter = index + 1
                    Card(
                        modifier =
                            Modifier.padding(4.dp).clickable { onChapterClick(chapter) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = chapter.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}
