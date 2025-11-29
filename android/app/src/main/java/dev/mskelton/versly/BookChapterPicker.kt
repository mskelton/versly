package dev.mskelton.versly

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.mskelton.versly.persistence.BookMetadata
import dev.mskelton.versly.persistence.LocalBibleDatabase
import dev.mskelton.versly.persistence.PassageId
import dev.mskelton.versly.ui.theme.VerslyTheme
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class Testament {
    OLD,
    NEW,
}

@Composable
fun BookChapterPicker(passageId: PassageId, onSelect: (book: String, chapter: String) -> Unit) {
    val bibleDatabase = LocalBibleDatabase.current

    var books by remember { mutableStateOf<List<BookMetadata>>(emptyList()) }
    var selectedTestament by remember { mutableIntStateOf(Testament.OLD.ordinal) }
    var selectedBook by remember { mutableStateOf<String?>(null) }

    val filteredBooks by remember {
        derivedStateOf {
            if (selectedTestament == Testament.OLD.ordinal) books.take(39) else books.drop(39)
        }
    }

    LaunchedEffect(passageId.translation) {
        withContext(Dispatchers.IO) { books = bibleDatabase.getBookList(passageId.translation) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SecondaryTabRow(selectedTabIndex = selectedTestament) {
            Tab(
                selected = selectedTestament == Testament.OLD.ordinal,
                onClick = { selectedTestament = Testament.OLD.ordinal },
                text = { Text(stringResource(R.string.old_testament)) },
            )
            Tab(
                selected = selectedTestament == Testament.NEW.ordinal,
                onClick = { selectedTestament = Testament.NEW.ordinal },
                text = { Text(stringResource(R.string.new_testament)) },
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            filteredBooks.forEach { book ->
                BookRow(
                    book = book,
                    isExpanded = selectedBook == book.id,
                    onBookClick = { selectedBook = if (selectedBook == book.id) null else book.id },
                    onChapterClick = { chapter -> onSelect(book.id, chapter.toString()) },
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
                Text(
                    text = stringResource(R.string.chapters, book.chapterCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
fun ChapterGrid(count: Int, onChapterClick: (Int) -> Unit) {
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
fun BookChapterPickerSheet(passageId: PassageId) {
    val backStack = LocalBackStack.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = { backStack.removeLastOrNull() }, sheetState = sheetState) {
        BookChapterPicker(
            passageId = passageId,
            onSelect = { book, chapter ->
                backStack.replace(Read(passageId = passageId.copy(book = book, chapter = chapter)))
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
