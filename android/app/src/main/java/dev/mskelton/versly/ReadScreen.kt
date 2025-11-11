package dev.mskelton.versly

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.mskelton.versly.persistence.BibleDatabase
import dev.mskelton.versly.persistence.BookMetadata
import dev.mskelton.versly.persistence.LocalAppPreferences
import dev.mskelton.versly.persistence.LocalBibleDatabase
import dev.mskelton.versly.persistence.Passage
import dev.mskelton.versly.persistence.PassageId
import dev.mskelton.versly.persistence.ReadViewModel
import dev.mskelton.versly.persistence.ReadViewModelFactory
import kotlinx.coroutines.launch

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
    val passageId by appPreferences.passage.collectAsState(initial = null)

    if (passageId == null) {
        LoadingSpinner()
    } else {
        ReadScreenContent(passageId!!)
    }
}

@Composable
fun ReadScreenContent(passageId: PassageId) {
    val bibleDatabase = LocalBibleDatabase.current
    val appPreferences = LocalAppPreferences.current

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val viewModel: ReadViewModel = viewModel(factory = ReadViewModelFactory(bibleDatabase))
    val passages by viewModel.passages.collectAsState()
    val nodes by viewModel.nodes.collectAsState()

    var books by remember { mutableStateOf<List<BookMetadata>>(emptyList()) }
    var totalChapters by remember { mutableIntStateOf(0) }
    var showBookPicker by remember { mutableStateOf(false) }
    var showChapterPicker by remember { mutableStateOf(false) }
    var showTranslationPicker by remember { mutableStateOf(false) }

    var book by remember { mutableStateOf("") }

    val toolbarVisibility = LocalToolbarVisibility.current

    // val isScrollingDown by remember {
    //     derivedStateOf {
    //         val firstVisibleItemIndex = listState.firstVisibleItemIndex
    //         val firstVisibleItemScrollOffset = listState.firstVisibleItemScrollOffset
    //
    //         firstVisibleItemIndex > 0 || firstVisibleItemScrollOffset > 100
    //     }
    // }
    //
    // LaunchedEffect(isScrollingDown) { toolbarVisibility.value = !isScrollingDown }

    LaunchedEffect(passageId) {
        books = bibleDatabase.getBookList(passageId.translation)
        viewModel.setPassageIds(listOf(passageId))
        listState.scrollToItem(0)
    }

    if (showBookPicker) {
        BookPicker(
            books = books,
            onBookSelected = {
                totalChapters = it.chapterCount
                showChapterPicker = it.chapterCount > 1
                showBookPicker = false

                scope.launch {
                    book = it.id

                    if (!showChapterPicker) {
                        appPreferences.setPassage(passageId.copy(book = it.id, chapter = "1"))
                    }
                }
            },
        )
    } else if (showChapterPicker) {
        GridPicker(
            items = (1..totalChapters).map { it.toString() },
            label = { it },
            onItemSelected = {
                scope.launch {
                    showChapterPicker = false
                    appPreferences.setPassage(passageId.copy(book = book, chapter = it))
                }
            },
        )
    } else if (showTranslationPicker) {
        TranslationPicker(onSelect = { showTranslationPicker = false })
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            // nodes.let { nodes ->
            //     LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
            //         items(nodes.size, key = { index -> nodes[index].id }) { index ->
            //             ReaderNode(nodes[index])
            //         }
            //
            //         item { Spacer(modifier = Modifier.height(120.dp)) }
            //     }
            // }

            Surface(color = MaterialTheme.colorScheme.background) { Text(text = "hi") }
            Surface(color = MaterialTheme.colorScheme.surfaceContainer) { Text(text = "hi") }
            Surface(color = MaterialTheme.colorScheme.surfaceContainer) { Text(text = "hi") }

            val surfaceBright: Color,
            val surfaceDim: Color,
            val surfaceContainer: Color,
            val surfaceContainerHigh: Color,
            val surfaceContainerHighest: Color,
            val surfaceContainerLow: Color,
            val surfaceContainerLowest: Color,
            Surface(color = MaterialTheme.colorScheme.background) { Text(text = "hi") }
            Surface(color = MaterialTheme.colorScheme.background) { Text(text = "hi") }
            Surface(color = MaterialTheme.colorScheme.background) { Text(text = "hi") }
            Surface(color = MaterialTheme.colorScheme.background) { Text(text = "hi") }
            Surface(color = MaterialTheme.colorScheme.background) { Text(text = "hi") }
            Surface(color = MaterialTheme.colorScheme.background) { Text(text = "hi") }
            Surface(color = MaterialTheme.colorScheme.background) { Text(text = "hi") }
            Surface(color = MaterialTheme.colorScheme.background) { Text(text = "hi") }

            AnimatedVisibility(
                visible = toolbarVisibility.value,
                // enter = slideInVertically { it },
                // exit = slideOutVertically { it },
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                val bookTitle = books.find { it.id == passageId.book }?.title ?: passageId.book

                ReadToolbar(
                    text = "$bookTitle ${passageId.chapter}",
                    translation = passageId.translation,
                    onSelectPassage = { showBookPicker = true },
                    onSelectTranslation = { showTranslationPicker = true },
                    onNavigateToPrevious = {
                        scope.launch {
                            val firstPassage = passages.firstOrNull() ?: return@launch
                            val passage = loadPreviousChapter(bibleDatabase, firstPassage)

                            if (passage != null) {
                                appPreferences.setPassage(passage.id)
                            }
                        }
                    },
                    onNavigateToNext = {
                        scope.launch {
                            val firstPassage = passages.firstOrNull() ?: return@launch
                            val passage = loadNextChapter(bibleDatabase, firstPassage)

                            if (passage != null) {
                                appPreferences.setPassage(passage.id)
                            }
                        }
                    },
                )
            }
        }
    }
}
