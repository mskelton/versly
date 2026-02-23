package dev.mskelton.versly

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.mskelton.versly.persistence.Passage
import dev.mskelton.versly.persistence.PassageId
import dev.mskelton.versly.persistence.PlansViewModel

@Composable
fun PlansScreen(viewModel: PlansViewModel) {
    val passages by viewModel.passages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val nodes by viewModel.nodes.collectAsState()
    val dayNumber by viewModel.dayNumber.collectAsState()

    val listState = rememberLazyListState()

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
        LazyColumn(modifier = Modifier.padding(horizontal = 16.dp), state = listState) {
            item {
                PlanCover(
                    modifier = Modifier.fillParentMaxHeight(),
                    dayNumber = dayNumber,
                    passages = passages,
                )
            }
            items(count = nodes.size, key = { index -> nodes[index].id }) { index ->
                ReaderNode(nodes[index])
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
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

        // Multiple consecutive chapters - last passage may be a partial chapter
        val lastRange = lastPassage.id.range
        return if (lastRange != null) {
            val endVerse = lastRange.getOrNull(1) ?: lastRange[0]
            "$bookTitle $startChapter-$endChapter:$endVerse"
        } else {
            "$bookTitle $startChapter-$endChapter"
        }
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

        // Check if this passage is consecutive with the previous one (same book, previous must be
        // a full chapter, and chapters are sequential). The current passage may be a full chapter
        // or a partial chapter (e.g., Romans 4 + Romans 5:1-11 → Romans 4-5:11).
        val isConsecutive =
            current.book == previous.book &&
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
fun PlanCover(
    modifier: Modifier = Modifier,
    dayNumber: Int?,
    passages: List<Passage>,
) {
    val groups = groupPassages(passages)

    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (dayNumber != null) {
            Text(
                text = stringResource(R.string.day_number, dayNumber),
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            groups.forEach { group ->
                Text(
                    text = group.format(),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview
@Composable
fun PlanCoverPreview() {
    PlanCover(
        dayNumber = 53,
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
    )
}
