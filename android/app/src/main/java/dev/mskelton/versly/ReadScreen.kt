package dev.mskelton.versly

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.mskelton.versly.persistence.LocalBibleDatabase
import dev.mskelton.versly.persistence.Passage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ReadScreen() {
    val bibleDatabase = LocalBibleDatabase.current
    var passage by remember { mutableStateOf<Passage?>(null) }
    val passageId = "JHN.3.${DEFAULT_TRANSLATION}"

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            passage = bibleDatabase.getPassage(passageId)
        }
    }

    passage?.let {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Reader(passage = it)
        }
    }
}
