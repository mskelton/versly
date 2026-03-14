package dev.mskelton.versly

import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.mskelton.versly.persistence.Passage
import dev.mskelton.versly.persistence.PassageId
import dev.mskelton.versly.persistence.PlansViewModel
import kotlinx.coroutines.launch

@Composable
fun PlansScreen(viewModel: PlansViewModel) {
    val passages by viewModel.passages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val nodes by viewModel.nodes.collectAsState()
    val dayNumber by viewModel.dayNumber.collectAsState()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

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
        LazyColumn(modifier = Modifier.padding(horizontal = 16.dp).testTag("plansList"), state = listState) {
            item {
                PlanCover(
                    modifier = Modifier.fillParentMaxHeight(),
                    dayNumber = dayNumber,
                    passages = passages,
                    onPassageClick = { passageIndex ->
                        val nodeIndex = passages.take(passageIndex).sumOf { it.nodes.size }
                        scope.launch { listState.animateScrollToItem(1 + nodeIndex) }
                    },
                )
            }
            items(count = nodes.size, key = { index -> nodes[index].id }) { index ->
                ReaderNode(nodes[index])
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

fun Passage.format(): String {
    val range = id.range
    if (range != null) {
        val start = range[0]
        val end = range.getOrNull(1)
        return if (end != null && start != end) {
            "$bookTitle $chapter:$start-$end"
        } else {
            "$bookTitle $chapter:$start"
        }
    }
    return "$bookTitle $chapter"
}

@Composable
fun PlanCover(
    modifier: Modifier = Modifier,
    dayNumber: Int?,
    passages: List<Passage>,
    onPassageClick: (Int) -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 32.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        if (dayNumber != null) {
            Text(
                text = stringResource(R.string.day_number, dayNumber),
                style = MaterialTheme.typography.displayMedium,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            passages.forEachIndexed { index, passage ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onPassageClick(index) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = passage.format(),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
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
