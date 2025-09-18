package dev.mskelton.versly

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
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

    val selectedTranslation by appPreferences.selectedTranslation.collectAsState(initial = "")
    var translations by remember { mutableStateOf<List<Translation>>(emptyList()) }
    var downloadingTranslation by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) { translations = bibleDatabase.getAvailableTranslations() }
    }

    val downloadedTranslations = translations.filter { it.isDownloaded }
    val remoteTranslations = translations.filter { !it.isDownloaded }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        if (downloadedTranslations.isNotEmpty()) {
            Text(
                text = "Downloaded translations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp),
            )

            downloadedTranslations.forEach { translation ->
                TranslationRow(
                    translation = translation,
                    isSelected = selectedTranslation == translation.id,
                    isDownloading = downloadingTranslation == translation.id,
                    onSelect = {
                        scope.launch {
                            appPreferences.setSelectedTranslation(translation.id)
                            onSelect()
                        }
                    },
                )
            }

            if (remoteTranslations.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp, horizontal = 4.dp))
            }
        }

        if (remoteTranslations.isNotEmpty()) {
            Text(
                text = "Available for download",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp),
            )

            remoteTranslations.forEach { translation ->
                TranslationRow(
                    translation = translation,
                    isSelected = selectedTranslation == translation.id,
                    isDownloading = downloadingTranslation == translation.id,
                    onSelect = {
                        scope.launch {
                            downloadingTranslation = translation.id
                            bibleDatabase.downloadTranslation(translation.id)
                            appPreferences.setSelectedTranslation(translation.id)
                            onSelect()
                        }
                    },
                )
            }
        }
    }
}

@Composable
fun TranslationRow(
    translation: Translation,
    isSelected: Boolean,
    isDownloading: Boolean,
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

        if (isDownloading) {
            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
        }
    }
}
