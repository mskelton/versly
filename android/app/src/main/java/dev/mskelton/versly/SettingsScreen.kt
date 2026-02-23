package dev.mskelton.versly

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.mskelton.versly.persistence.LocalAppPreferences
import dev.mskelton.versly.persistence.SettingsViewModel
import dev.mskelton.versly.persistence.Translation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val appPreferences = LocalAppPreferences.current

    val currentTranslation by appPreferences.translation.collectAsState(initial = "ESV")
    val translations by viewModel.translations.collectAsState()
    val loadingTranslation by viewModel.loadingTranslation.collectAsState()

    val downloadedTranslations by remember {
        derivedStateOf { translations.filter { it.isDownloaded } }
    }
    val remoteTranslations by remember {
        derivedStateOf { translations.filter { !it.isDownloaded } }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(stringResource(R.string.settings)) }, windowInsets = WindowInsets())

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.translations),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            if (downloadedTranslations.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.downloaded_translations),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                downloadedTranslations.forEach { translation ->
                    TranslationManagementRow(
                        translation = translation,
                        isLoading = loadingTranslation == translation.id,
                        isCurrentTranslation = currentTranslation == translation.id,
                        onDelete = { viewModel.deleteTranslation(translation.id) },
                    )
                }

                if (remoteTranslations.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                }
            }

            if (remoteTranslations.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.available_for_download),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                remoteTranslations.forEach { translation ->
                    TranslationManagementRow(
                        translation = translation,
                        isLoading = loadingTranslation == translation.id,
                        isCurrentTranslation = false,
                        onDownload = { viewModel.downloadTranslation(translation.id) },
                    )
                }
            }
        }
    }
}

@Composable
fun TranslationManagementRow(
    translation: Translation,
    isLoading: Boolean,
    isCurrentTranslation: Boolean,
    onDownload: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = translation.id,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )

            Text(
                text = translation.title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (isLoading) {
            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
        } else if (translation.isDownloaded) {
            OutlinedButton(onClick = { onDelete?.invoke() }, enabled = !isCurrentTranslation) {
                Text(stringResource(R.string.delete))
            }
        } else {
            Button(onClick = { onDownload?.invoke() }) { Text(stringResource(R.string.download)) }
        }
    }
}
