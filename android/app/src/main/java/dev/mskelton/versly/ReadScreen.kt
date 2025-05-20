package dev.mskelton.versly

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
    var passage by remember { mutableStateOf<Passage?>(null) }
    val passageId = "NUM.1.GNT"

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            passage = bibleDatabase.getPassage(passageId)
        }
    }

    passage?.let {
        Box(modifier = Modifier.verticalScroll(scrollState)) {
            Box(modifier = Modifier.padding(16.dp, 32.dp)) {
                Reader(passage = it)
            }
        }
    }
}

