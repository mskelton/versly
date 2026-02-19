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
import dev.mskelton.versly.persistence.LocalAppPreferences
import dev.mskelton.versly.persistence.Passage
import dev.mskelton.versly.persistence.PassageId
import dev.mskelton.versly.persistence.PlansViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDate

@Composable
fun PlansScreen(viewModel: PlansViewModel) {
    val context = LocalContext.current
    val appPreferences = LocalAppPreferences.current

    val translation by appPreferences.translation.collectAsState(initial = null)

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
                        val rangeObj = it.optJSONObject("range")
                        val range =
                            if (rangeObj != null) {
                                val start = rangeObj.getInt("start")
                                val end = rangeObj.getInt("end")
                                listOf(start.toString(), end.toString())
                            } else {
                                null
                            }

                        PassageId(
                            book = it.getString("book"),
                            chapter = it.getString("chapter"),
                            translation = translation!!,
                            range = range,
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

/**
 * Represents a group of consecutive passages from the same book that can be displayed as a single
 * chip.
 */
data class PassageGroup(
    val passages: List<Passage>,
) {
    val firstPassage: Passage
        get() = passages.first()

    val lastPassage: Passage
        get() = passages.last()

    /**
     * Formats the passage group as a display string (e.g., "Psalms 110-113" or "Genesis 1:5-10")
     */
    fun format(): String {
        val bookTitle = firstPassage.bookTitle
        val startChapter = firstPassage.chapter
        val endChapter = lastPassage.chapter

        // Single passage - may have verse range
        if (passages.size == 1) {
            val range = firstPassage.id.range
            if (range != null) {
                val start = range[0]
                val end = range.getOrNull(1)
                return if (end != null && start != end) {
                    "$bookTitle $startChapter:$start-$end"
                } else {
                    "$bookTitle $startChapter:$start"
                }
            }
            return "$bookTitle $startChapter"
        }

        // Multiple consecutive chapters
        return "$bookTitle $startChapter-$endChapter"
    }
}

/** Groups consecutive passages from the same book into PassageGroups. */
fun groupPassages(passages: List<Passage>): List<PassageGroup> {
    if (passages.isEmpty()) return emptyList()

    val groups = mutableListOf<PassageGroup>()
    var currentGroup = mutableListOf(passages.first())

    for (i in 1 until passages.size) {
        val current = passages[i]
        val previous = currentGroup.last()

        // Check if this passage is consecutive with the previous one (same book, no verse ranges,
        // and chapters are sequential)
        val isConsecutive =
            current.book == previous.book &&
                current.id.range == null &&
                previous.id.range == null &&
                current.chapter.toIntOrNull() == (previous.chapter.toIntOrNull()?.plus(1))

        if (isConsecutive) {
            currentGroup.add(current)
        } else {
            groups.add(PassageGroup(currentGroup.toList()))
            currentGroup = mutableListOf(current)
        }
    }

    // Add the last group
    groups.add(PassageGroup(currentGroup.toList()))

    return groups
}

@Composable
fun PlanPreview(
    passages: List<Passage>,
    onSelect: (passage: Passage) -> Unit,
) {
    val groups = groupPassages(passages)

    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
    ) {
        groups.forEach { group ->
            Surface(
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier.padding(horizontal = 4.dp),
                onClick = { onSelect(group.firstPassage) },
            ) {
                Text(
                    text = group.format(),
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
                // Genesis 1 - single chapter
                Passage(
                    id = PassageId("GEN", "1", "KJV"),
                    book = "GEN",
                    bookTitle = "Genesis",
                    bookAbbreviation = "Gen",
                    chapter = "1",
                    translation = "KJV",
                    nodes = emptyList(),
                ),
                // Psalms 110-113 - consecutive chapters that should be grouped
                Passage(
                    id = PassageId("PSA", "110", "KJV"),
                    book = "PSA",
                    bookTitle = "Psalms",
                    bookAbbreviation = "Psa",
                    chapter = "110",
                    translation = "KJV",
                    nodes = emptyList(),
                ),
                Passage(
                    id = PassageId("PSA", "111", "KJV"),
                    book = "PSA",
                    bookTitle = "Psalms",
                    bookAbbreviation = "Psa",
                    chapter = "111",
                    translation = "KJV",
                    nodes = emptyList(),
                ),
                Passage(
                    id = PassageId("PSA", "112", "KJV"),
                    book = "PSA",
                    bookTitle = "Psalms",
                    bookAbbreviation = "Psa",
                    chapter = "112",
                    translation = "KJV",
                    nodes = emptyList(),
                ),
                Passage(
                    id = PassageId("PSA", "113", "KJV"),
                    book = "PSA",
                    bookTitle = "Psalms",
                    bookAbbreviation = "Psa",
                    chapter = "113",
                    translation = "KJV",
                    nodes = emptyList(),
                ),
                // Matthew 5:1-16 - single chapter with verse range
                Passage(
                    id = PassageId("MAT", "5", "KJV", range = listOf("1", "16")),
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
