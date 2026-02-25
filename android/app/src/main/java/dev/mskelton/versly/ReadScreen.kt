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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.mskelton.versly.persistence.LocalAppPreferences
import dev.mskelton.versly.persistence.PassageId
import dev.mskelton.versly.persistence.ReadViewModel
import kotlinx.coroutines.FlowPreview

@Composable
fun ReadScreen(viewModel: ReadViewModel) {
    val appPreferences = LocalAppPreferences.current

    val savedPassageId by appPreferences.passage.collectAsState(null)
    val currentPassageId by viewModel.currentPassageId.collectAsState()

    // Always use the translation from savedPassageId so that when the user changes
    // their translation preference, the passage updates to the new translation.
    val id: PassageId? =
        savedPassageId?.let { saved ->
            val base = currentPassageId ?: saved
            PassageId(base.book, base.chapter, saved.translation, base.range)
        } ?: currentPassageId

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
    val backStack = LocalBackStack.current

    val slideDirection by viewModel.slideDirection.collectAsState()
    val nodes by viewModel.nodesFor(passageId).collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = passageId,
            transitionSpec = { horizontalSlideTransition(slideDirection) },
            label = "chapter",
        ) { targetPassageId ->
            val targetNodes by viewModel.nodesFor(targetPassageId).collectAsState()

            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                items(targetNodes.size, key = { index -> targetNodes[index].id }) { index ->
                    ReaderNode(targetNodes[index])
                }

                item { Spacer(modifier = Modifier.height(120.dp)) }
            }
        }

        val bookTitle = viewModel.getBookTitle(passageId, nodes)

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
