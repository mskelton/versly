package dev.mskelton.versly

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.mskelton.versly.persistence.BookMetadata
import dev.mskelton.versly.persistence.LocalAppPreferences
import dev.mskelton.versly.persistence.LocalBibleDatabase
import dev.mskelton.versly.persistence.PassageId
import dev.mskelton.versly.persistence.ReadViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.withContext

@Composable
fun ReadScreen(viewModel: ReadViewModel) {
    val appPreferences = LocalAppPreferences.current

    val savedPassageId by appPreferences.passage.collectAsState(null)
    val currentPassageId by viewModel.currentPassageId.collectAsState()

    val id = currentPassageId ?: savedPassageId

    LaunchedEffect(id) {
        val resolvedId = id ?: return@LaunchedEffect
        viewModel.setCurrentPassage(resolvedId)
        appPreferences.setPassage(resolvedId)
    }

    if (id == null) {
        LoadingSpinner()
    } else {
        ReadScreenContent(
            passageId = id,
            viewModel = viewModel,
        )
    }
}

@OptIn(FlowPreview::class)
@Composable
fun ReadScreenContent(
    passageId: PassageId,
    viewModel: ReadViewModel,
) {
    val bibleDatabase = LocalBibleDatabase.current
    val backStack = LocalBackStack.current

    val slideDirection by viewModel.slideDirection.collectAsState()

    var books by remember { mutableStateOf<List<BookMetadata>>(emptyList()) }

    LaunchedEffect(passageId) {
        books = withContext(Dispatchers.IO) { bibleDatabase.getBookList(passageId.translation) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = passageId,
            transitionSpec = { horizontalSlideTransition(slideDirection) },
            label = "chapter",
        ) { targetPassageId ->
            val nodes by viewModel.nodesFor(targetPassageId).collectAsState()

            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                items(nodes.size, key = { index -> nodes[index].id }) { index ->
                    ReaderNode(nodes[index])
                }

                item { Spacer(modifier = Modifier.height(120.dp)) }
            }
        }

        val bookTitle = books.find { it.id == passageId.book }?.title ?: passageId.book

        ReaderToolbar(
            modifier = Modifier.align(Alignment.BottomCenter),
            text = "$bookTitle ${passageId.chapter}",
            translation = passageId.translation,
            onSelectPassage = { backStack.add(PickPassage(passageId)) },
            onSelectTranslation = { backStack.addSheet(PickTranslationSheet) },
            onNavigateToPrevious = { viewModel.navigatePrevious() },
            onNavigateToNext = { viewModel.navigateNext() },
        )
    }
}
