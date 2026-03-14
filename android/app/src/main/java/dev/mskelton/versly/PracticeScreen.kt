package dev.mskelton.versly

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.Alignment
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.mskelton.versly.persistence.LocalVerslyDatabase
import dev.mskelton.versly.persistence.MemoryVerse
import dev.mskelton.versly.persistence.Node
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    verse: MemoryVerse,
    onDelete: () -> Unit,
) {
    val backStack = LocalBackStack.current
    val db = LocalVerslyDatabase.current
    var nodes by remember { mutableStateOf<List<Node>>(emptyList()) }
    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(verse) {
        withContext(Dispatchers.IO) {
            val range =
                if (verse.verseEnd != null && verse.verseEnd != verse.verseStart) {
                    listOf(verse.verseStart, verse.verseEnd)
                } else {
                    listOf(verse.verseStart)
                }
            val passage = db.getPassage(verse.book, verse.chapter, verse.translation, range, includeChapterNode = false)
            nodes = passage.nodes
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text(verse.reference) },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(
                            painter = painterResource(R.drawable.chevron_left_24px),
                            contentDescription = stringResource(R.string.navigate_up),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            painter = painterResource(R.drawable.more_vert_24px),
                            contentDescription = stringResource(R.string.more_options),
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete_verse)) },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.delete_24px),
                                    contentDescription = null,
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                                backStack.removeLastOrNull()
                            },
                        )
                    }
                },
                windowInsets = WindowInsets(),
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
            ) {
                items(nodes.size, key = { index -> nodes[index].id }) { index ->
                    ReaderNode(nodes[index])
                }
            }

            TextToSpeechPlayer(
                text = verse.text,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
