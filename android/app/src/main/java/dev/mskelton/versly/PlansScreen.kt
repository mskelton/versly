package dev.mskelton.versly

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.mskelton.versly.persistence.LocalBibleDatabase
import dev.mskelton.versly.persistence.Passage
import dev.mskelton.versly.persistence.PassageId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDate

@Composable
fun PlansScreen() {
    val context = LocalContext.current
    val bibleDatabase = LocalBibleDatabase.current
    val scrollState = rememberScrollState()
    var passages by rememberSaveable { mutableStateOf<List<Passage>?>(null) }

    LaunchedEffect(context) {
        withContext(Dispatchers.IO) {
            val today = LocalDate.now().toString()
            val days = context.assets
                .open("plan.json")
                .bufferedReader()
                .use { JSONObject(it.readText()) }
                .getJSONObject("plan")
                .getJSONArray("days")

            val day = (0 until days.length())
                .map { days.getJSONObject(it) }
                .find { it.getString("date") == today }

            val readings = day?.getJSONArray("readings")
            passages =
                (0 until (readings?.length() ?: 0)).map { readings!!.getJSONObject(it) }.map {
                    bibleDatabase.getPassage(
                        PassageId(
                            book = it.getString("book"),
                            chapter = it.getString("chapter"),
                            translation = DEFAULT_TRANSLATION,
                        )
                    )
                }
        }
    }

    if (passages == null) {
        return Box(
            modifier = Modifier
                .padding(16.dp, 32.dp)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp,
            )
        }
    } else if (passages!!.isEmpty()) {
        Text(
            text = stringResource(R.string.no_readings_for_today),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp, 32.dp)
        )
    } else {
        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(8.dp),
            ) {
                passages!!.forEach {
                    Text("${it.bookAbbreviation} ${it.id.chapter}")
                }
            }

            Box(modifier = Modifier.verticalScroll(scrollState)) {
                Column(
                    modifier = Modifier.padding(16.dp, 32.dp),
                    verticalArrangement = Arrangement.spacedBy(80.dp)
                ) {
                    passages!!.forEach { Reader(it) }
                }
            }
        }
    }
}
