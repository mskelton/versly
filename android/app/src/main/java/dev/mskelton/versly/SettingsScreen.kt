package dev.mskelton.versly

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.mskelton.versly.persistence.LocalAppPreferences
import dev.mskelton.versly.persistence.SettingsViewModel
import dev.mskelton.versly.persistence.Translation
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val appPreferences = LocalAppPreferences.current
    val coroutineScope = rememberCoroutineScope()

    val currentTranslation by appPreferences.translation.collectAsState(initial = "ESV")
    val translations by viewModel.translations.collectAsState()
    val loadingTranslation by viewModel.loadingTranslation.collectAsState()

    val sortedTranslations by remember {
        derivedStateOf { translations.sortedByDescending { it.isDownloaded } }
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
                modifier = Modifier.padding(bottom = 8.dp),
            )

            sortedTranslations.forEach { translation ->
                TranslationManagementRow(
                    translation = translation,
                    isLoading = loadingTranslation == translation.id,
                    isCurrentTranslation = currentTranslation == translation.id,
                    onSelect =
                        if (translation.isDownloaded) {
                            { coroutineScope.launch { appPreferences.setTranslation(translation.id) } }
                        } else {
                            null
                        },
                    onDownload = { viewModel.downloadTranslation(translation.id) },
                    onDelete = { viewModel.deleteTranslation(translation.id) },
                )
            }
        }
    }
}

@Composable
fun TranslationManagementRow(
    translation: Translation,
    isLoading: Boolean,
    isCurrentTranslation: Boolean,
    onSelect: (() -> Unit)? = null,
    onDownload: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    val isSelectable = onSelect != null && !isCurrentTranslation
    val shape = RoundedCornerShape(12.dp)

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .then(
                    if (isSelectable) {
                        Modifier.clickable { onSelect?.invoke() }
                    } else {
                        Modifier
                    },
                ).padding(vertical = 4.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = translation.id,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )

                if (isCurrentTranslation) {
                    Icon(
                        painter = painterResource(R.drawable.check_24px),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 6.dp).size(18.dp),
                    )
                }
            }

            Text(
                text = translation.title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
            }
        } else if (translation.isDownloaded) {
            IconButton(
                onClick = { onDelete?.invoke() },
                enabled = !isCurrentTranslation,
            ) {
                Icon(
                    painter = painterResource(R.drawable.delete_24px),
                    contentDescription = stringResource(R.string.delete_translation),
                )
            }
        } else {
            IconButton(onClick = { onDownload?.invoke() }) {
                Icon(
                    painter = painterResource(R.drawable.download_24px),
                    contentDescription = stringResource(R.string.download_translation),
                )
            }
        }
    }
}
