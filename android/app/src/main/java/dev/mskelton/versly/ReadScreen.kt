package dev.mskelton.versly

import android.R.attr.text
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.mskelton.versly.persistence.BibleDatabase
import dev.mskelton.versly.persistence.BookMetadata
import dev.mskelton.versly.persistence.LocalAppPreferences
import dev.mskelton.versly.persistence.LocalBibleDatabase
import dev.mskelton.versly.persistence.Passage
import dev.mskelton.versly.persistence.PassageId
import dev.mskelton.versly.persistence.ReadViewModel
import dev.mskelton.versly.persistence.ReadViewModelFactory
import kotlinx.coroutines.FlowPreview
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
fun ReadScreen(
    passageId: PassageId?,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    val appPreferences = LocalAppPreferences.current
    val savedPassageId = appPreferences.passage.collectAsState(null)
    val id = passageId ?: savedPassageId.value

    LaunchedEffect(passageId) {
        if (passageId != null) {
            appPreferences.setPassage(passageId)
        }
    }

    if (id == null) {
        LoadingSpinner()
    } else {
        ReadScreenContent(id, sharedTransitionScope, animatedContentScope)
    }
}

@OptIn(FlowPreview::class)
@Composable
fun ReadScreenContent(
    passageId: PassageId,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
) {
    val bibleDatabase = LocalBibleDatabase.current
    val backStack = LocalBackStack.current

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val viewModel: ReadViewModel = viewModel(factory = ReadViewModelFactory(bibleDatabase))
    val passages by viewModel.passages.collectAsState()
    val nodes by viewModel.nodes.collectAsState()

    var books by remember { mutableStateOf<List<BookMetadata>>(emptyList()) }

    LaunchedEffect(passageId) {
        books = bibleDatabase.getBookList(passageId.translation)
        viewModel.setPassageIds(listOf(passageId))
        listState.scrollToItem(0)
    }

    // LaunchedEffect(listState) {
    //     var lastOffset = listState.firstVisibleItemScrollOffset
    //     var lastDirection = 0
    //
    //     snapshotFlow { listState.firstVisibleItemScrollOffset }
    //         .collect { offset ->
    //             // Scroll up
    //             if (lastDirection >= 0 && offset < lastOffset) {
    //                 toolbarVisibility.value = true
    //                 lastDirection = -1
    //                 lastOffset = offset
    //             }
    //             // Scroll down
    //             else if (lastDirection <= 0 && offset > lastOffset) {
    //                 toolbarVisibility.value = false
    //                 lastDirection = 1
    //                 lastOffset = offset
    //             }
    //         }
    // }

    Box(modifier = Modifier.fillMaxSize().animateContentSize()) {
        nodes.let { nodes ->
            LazyColumn(state = listState, modifier = Modifier.padding(horizontal = 16.dp)) {
                items(nodes.size, key = { index -> nodes[index].id }) { index ->
                    ReaderNode(nodes[index])
                }

                item { Spacer(modifier = Modifier.height(120.dp)) }
            }
        }

        val bookTitle = books.find { it.id == passageId.book }?.title ?: passageId.book

        with(sharedTransitionScope) {
            ReaderToolbar(
                modifier =
                    Modifier.align(Alignment.BottomCenter)
                        .sharedElement(
                            sharedTransitionScope.rememberSharedContentState(key = "ReaderToolbar"),
                            animatedVisibilityScope = animatedContentScope,
                        ),
                text = "$bookTitle ${passageId.chapter}",
                translation = passageId.translation,
                onSelectPassage = { backStack.addSheet(PickPassageSheet(passageId)) },
                onSelectTranslation = { backStack.addSheet(PickTranslationSheet) },
                onNavigateToPrevious = {
                    scope.launch {
                        val firstPassage = passages.firstOrNull() ?: return@launch
                        val passage = loadPreviousChapter(bibleDatabase, firstPassage)

                        if (passage != null) {
                            backStack.replace(Read(passage.id))
                        }
                    }
                },
                onNavigateToNext = {
                    scope.launch {
                        val firstPassage = passages.firstOrNull() ?: return@launch
                        val passage = loadNextChapter(bibleDatabase, firstPassage)

                        if (passage != null) {
                            backStack.replace(Read(passage.id))
                        }
                    }
                },
            )
        }
    }
}
