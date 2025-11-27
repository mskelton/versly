package dev.mskelton.versly

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.mskelton.versly.persistence.LocalAppPreferences
import dev.mskelton.versly.persistence.LocalBibleDatabase
import dev.mskelton.versly.persistence.Translation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TranslationPicker(onSelect: () -> Unit) {
    val appPreferences = LocalAppPreferences.current
    val bibleDatabase = LocalBibleDatabase.current
    val scope = rememberCoroutineScope()

    val passageId by appPreferences.passage.collectAsState(initial = null)
    var translations by remember { mutableStateOf<List<Translation>>(emptyList()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) { translations = bibleDatabase.getAvailableTranslations().filter { it.isDownloaded } }
    }

    if (passageId == null) {
        LoadingSpinner()
        return
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        translations.forEach { translation ->
            TranslationRow(
                translation = translation,
                isSelected = passageId!!.translation == translation.id,
                onSelect = {
                    scope.launch {
                        appPreferences.setPassage(
                            passageId!!.copy(translation = translation.id)
                        )
                        onSelect()
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationPickerSheet() {
    val backStack = LocalBackStack.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(onDismissRequest = { backStack.removeLastOrNull() }, sheetState = sheetState) {
        TranslationPicker(onSelect = { backStack.removeLastOrNull() })
    }
}

@Composable
fun TranslationRow(
    translation: Translation,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clickable { onSelect() }
                .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(selected = isSelected, onClick = { onSelect() })

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = translation.id,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            )

            Text(
                text = translation.title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
