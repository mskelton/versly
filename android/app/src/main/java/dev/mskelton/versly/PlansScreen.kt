package dev.mskelton.versly

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.mskelton.versly.persistence.LocalAppPreferences
import dev.mskelton.versly.persistence.LocalBibleDatabase
import dev.mskelton.versly.persistence.Passage
import dev.mskelton.versly.persistence.PassageId
import dev.mskelton.versly.persistence.PlansViewModel
import dev.mskelton.versly.persistence.PlansViewModelFactory
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

@Composable
fun PlansScreen() {
    val context = LocalContext.current
    val bibleDatabase = LocalBibleDatabase.current
    val appPreferences = LocalAppPreferences.current

    val translation by appPreferences.translation.collectAsState(initial = null)

    val viewModel: PlansViewModel = viewModel(factory = PlansViewModelFactory(bibleDatabase))
    val passages by viewModel.passages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val nodes by viewModel.nodes.collectAsState()

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(context, translation) {
        if (translation == null) return@LaunchedEffect

        withContext(Dispatchers.IO) {
            val today = LocalDate.now().toString()
            val days =
                context.assets
                    .open("plan.json")
                    .bufferedReader()
                    .use { JSONObject(it.readText()) }
                    .getJSONObject("plan")
                    .getJSONArray("days")

            val day =
                (0 until days.length())
                    .map { days.getJSONObject(it) }
                    .find { it.getString("date") == today }

            val readings = day?.getJSONArray("readings")
            val newPassageIds =
                (0 until (readings?.length() ?: 0))
                    .map { readings!!.getJSONObject(it) }
                    .map {
                        PassageId(
                            book = it.getString("book"),
                            chapter = it.getString("chapter"),
                            translation = translation!!,
                        )
                    }

            viewModel.setPassageIds(newPassageIds)
            viewModel.setLoading(false)
        }
    }

    if (isLoading) {
        LoadingSpinner()
    } else if (passages.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.no_readings_for_today),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp, 32.dp),
            )
        }
    } else {
        Column {
            PlanPreview(
                passages = passages,
                onSelect = { passage ->
                    val nodeId = "${passage.book}.${passage.chapter}.0"
                    val index = nodes.indexOfFirst { it.id == nodeId }
                    if (index != -1) {
                        scope.launch { listState.animateScrollToItem(index, 0) }
                    }
                },
            )

            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp), state = listState) {
                items(count = nodes.size, key = { index -> nodes[index].id }) { index ->
                    ReaderNode(nodes[index])
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun PlanPreview(passages: List<Passage>, onSelect: (passage: Passage) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
    ) {
        passages.forEach { passage ->
            Surface(
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier.padding(horizontal = 4.dp),
                onClick = { onSelect(passage) },
            ) {
                Text(
                    text = "${passage.bookTitle} ${passage.chapter}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Preview
@Composable
fun PlanPreviewPreview() {
    PlanPreview(
        passages =
            listOf(
                Passage(
                    id = PassageId("GEN", "1", "KJV"),
                    book = "GEN",
                    bookTitle = "Genesis",
                    bookAbbreviation = "Gen",
                    chapter = "1",
                    translation = "KJV",
                    nodes = emptyList(),
                ),
                Passage(
                    id = PassageId("PSA", "23", "KJV"),
                    book = "PSA",
                    bookTitle = "Psalms",
                    bookAbbreviation = "Psa",
                    chapter = "23",
                    translation = "KJV",
                    nodes = emptyList(),
                ),
                Passage(
                    id = PassageId("MAT", "5", "KJV"),
                    book = "MAT",
                    bookTitle = "Matthew",
                    bookAbbreviation = "Mat",
                    chapter = "5",
                    translation = "KJV",
                    nodes = emptyList(),
                ),
            ),
        onSelect = {},
    )
}
