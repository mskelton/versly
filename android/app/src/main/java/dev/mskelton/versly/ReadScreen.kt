package dev.mskelton.versly

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.mskelton.versly.persistence.BookMetadata
import dev.mskelton.versly.persistence.LocalBibleDatabase
import dev.mskelton.versly.persistence.Passage
import dev.mskelton.versly.persistence.PassageId
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
    val passageId = PassageId(
        book = selectedBook,
        chapter = selectedChapter,
        translation = DEFAULT_TRANSLATION,
    )

    LaunchedEffect(passageId) {
        withContext(Dispatchers.IO) {
            books = bibleDatabase.getBookList(DEFAULT_TRANSLATION)
            passage = bibleDatabase.getPassage(passageId)
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
        Box(modifier = Modifier.verticalScroll(scrollState)) {
            Column(modifier = Modifier.padding(16.dp, 32.dp)) {
                Row {
                    Text(
                        "Book: $selectedBook",
                        Modifier.clickable { showBookPicker.value = true },
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Chapter: $selectedChapter",
                        Modifier.clickable { showChapterPicker.value = true },
                    )
                }

                passage?.let {
                    Reader(passage = it)
                }
            }
        }
    }
}


