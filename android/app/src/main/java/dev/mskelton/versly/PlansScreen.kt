package dev.mskelton.versly

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.mskelton.versly.persistence.ChapterId
import dev.mskelton.versly.persistence.LocalAppPreferences
import dev.mskelton.versly.persistence.LocalBibleDatabase
import dev.mskelton.versly.persistence.Passage
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun PlansScreen() {
    val context = LocalContext.current
    val bibleDatabase = LocalBibleDatabase.current
    val appPreferences = LocalAppPreferences.current
    val scrollState = rememberScrollState()
    var passagesState by rememberSaveable { mutableStateOf<List<Passage>?>(null) }
    var nodesState by rememberSaveable { mutableStateOf<JSONArray?>(null) }

    val selectedTranslation by appPreferences.selectedTranslation.collectAsState(initial = "")

    LaunchedEffect(context, selectedTranslation) {
        if (selectedTranslation.isEmpty()) return@LaunchedEffect

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
            val passages =
                (0 until (readings?.length() ?: 0))
                    .map { readings!!.getJSONObject(it) }
                    .map {
                        bibleDatabase.getPassage(
                            ChapterId(
                                book = it.getString("book"),
                                chapter = it.getString("chapter"),
                                translation = selectedTranslation,
                            )
                        )
                    }

            val nodes = JSONArray()
            for (passage in passages) {
                for (i in 0 until passage.nodes.length()) {
                    nodes.put(passage.nodes.getJSONArray(i))
                }
            }

            passagesState = passages
            nodesState = nodes
        }
    }

    if (nodesState == null) {
        LoadingSpinner()
    } else if (nodesState!!.length() == 0) {
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(8.dp),
            ) {
                passagesState!!.forEach { Text("${it.bookAbbreviation} ${it.id.chapter}") }
            }

            nodesState!!.let {
                LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                    items(
                        it.length()
                        //                        key = { index ->
                        // "${it.id.chapter}.${it.id.book}.$index" },
                    ) { index ->
                        ReaderNode(it.getJSONArray(index))
                    }

                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
            //            Box(modifier = Modifier.verticalScroll(scrollState)) {
            //                Column(
            //                    modifier = Modifier.padding(16.dp, 32.dp),
            //                    verticalArrangement = Arrangement.spacedBy(80.dp),
            //                ) {
            //                    //                    passages!!.forEach { Reader(it) }
            //                }
            //            }
        }
    }
}
