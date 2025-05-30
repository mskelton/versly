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
import dev.mskelton.versly.persistence.LocalBibleDatabase
import dev.mskelton.versly.persistence.Passage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ReadScreen() {
    val bibleDatabase = LocalBibleDatabase.current
    val scrollState = rememberScrollState()
    var bookIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var passage by remember { mutableStateOf<Passage?>(null) }
    var selectedBook by remember { mutableStateOf("NUM") }
    var selectedChapter by remember { mutableIntStateOf(1) }
    val showBookPicker = remember { mutableStateOf(false) }
    val showChapterPicker = remember { mutableStateOf(false) }
    val passageId = "${selectedBook}.${selectedChapter}.${DEFAULT_TRANSLATION}"

    LaunchedEffect(passageId) {
        withContext(Dispatchers.IO) {
            bookIds = bibleDatabase.getBookIds(DEFAULT_TRANSLATION)
            passage = bibleDatabase.getPassage(passageId)
        }
    }

    if (showBookPicker.value) {
        GridPicker(
            items = bookIds,
            onItemSelected = {
                selectedBook = it
                selectedChapter = 1
                showBookPicker.value = false
                showChapterPicker.value = true
            },
        )
    } else if (showChapterPicker.value) {
        GridPicker(items = (1..36).map { it.toString() }, // Replace 36 with actual chapter count for selectedBook
            onItemSelected = {
                selectedChapter = it.toInt()
                showChapterPicker.value = false
            })
    } else {
        Box(modifier = Modifier.verticalScroll(scrollState)) {
            Column(modifier = Modifier.padding(16.dp, 32.dp)) {
                Row {
                    Text("Book: $selectedBook", Modifier.clickable { showBookPicker.value = true })
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


