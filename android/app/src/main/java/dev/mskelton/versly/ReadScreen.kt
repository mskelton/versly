package dev.mskelton.versly

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import dev.mskelton.versly.persistence.PassageId
import dev.mskelton.versly.persistence.ReadViewModel
import kotlinx.coroutines.FlowPreview

@Composable
fun ReadScreen(viewModel: ReadViewModel) {
    val currentPassageId by viewModel.currentPassageId.collectAsState()

    if (currentPassageId == null) {
        LoadingSpinner()
    } else {
        ReadScreenContent(
            passageId = currentPassageId!!,
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

    var horizontalOffset = remember { mutableFloatStateOf(0f) }
    var verticalOffset = remember { mutableFloatStateOf(0f) }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            val threshold = 100.dp.toPx()
                            val absX = kotlin.math.abs(horizontalOffset.floatValue)
                            val absY = kotlin.math.abs(verticalOffset.floatValue)
                            val isHorizontal = absX + absY > 0 && absX / (absX + absY) >= 0.6f

                            if (isHorizontal) {
                                when {
                                    horizontalOffset.floatValue < -threshold -> viewModel.navigateNext()
                                    horizontalOffset.floatValue > threshold -> viewModel.navigatePrevious()
                                }
                            }

                            horizontalOffset.floatValue = 0f
                            verticalOffset.floatValue = 0f
                        },
                        onDragCancel = {
                            horizontalOffset.floatValue = 0f
                            verticalOffset.floatValue = 0f
                        },
                        onDrag = { _, dragAmount ->
                            horizontalOffset.floatValue += dragAmount.x
                            verticalOffset.floatValue += dragAmount.y
                        },
                    )
                },
    ) {
        AnimatedContent(
            targetState = passageId,
            transitionSpec = {
                if (initialState.translation != targetState.translation) {
                    crossfadeTransition()
                } else {
                    horizontalSlideTransition(slideDirection)
                }
            },
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

        // Cache the old book title until we know the new one
        var displayedBookTitle by remember { mutableStateOf("") }
        val bookTitle = viewModel.getBookTitle(passageId, nodes)
        if (bookTitle != null) displayedBookTitle = bookTitle

        ReaderToolbar(
            modifier = Modifier.align(Alignment.BottomCenter),
            text = "$displayedBookTitle ${passageId.chapter}",
            translation = passageId.translation,
            onSelectPassage = { backStack.add(PickPassage(passageId)) },
            onSelectTranslation = { backStack.addSheet(PickTranslationSheet) },
            onNavigateToPrevious = { viewModel.navigatePrevious() },
            onNavigateToNext = { viewModel.navigateNext() },
        )
    }
}
