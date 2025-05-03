package dev.mskelton.versly

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.mskelton.versly.persistence.LocalBibleDatabase
import dev.mskelton.versly.persistence.Passage

@Composable
fun ReadScreen() {
    val bibleDatabase = LocalBibleDatabase.current
    var passage by remember { mutableStateOf<Passage?>(null) }

    LaunchedEffect(Unit) {
        passage = bibleDatabase.getPassage("JHN.3.$DEFAULT_TRANSLATION")
    }

    Text("Hi ${passage?.bookTitle}")
//    items[item].data.forEach { verse ->
//        Text(text = "${verse.verseNumber} ${verse.text}")
//    }
}
