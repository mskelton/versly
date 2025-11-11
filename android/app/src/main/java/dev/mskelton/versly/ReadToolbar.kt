package dev.mskelton.versly

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ReadToolbar(
    text: String,
    translation: String,
    onSelectPassage: () -> Unit,
    onSelectTranslation: () -> Unit,
    onNavigateToPrevious: () -> Unit,
    onNavigateToNext: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = RoundedCornerShape(32.dp),
        onClick = onSelectPassage,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(32.dp),
                onClick = onNavigateToPrevious,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.previous_chapter),
                    modifier = Modifier.size(38.dp),
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Left,
                )
            }

            Surface(
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.padding(2.dp),
                onClick = onSelectTranslation,
            ) {
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
                ) {
                    Text(text = translation, style = MaterialTheme.typography.titleMedium)
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(32.dp),
                onClick = onNavigateToNext,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.next_chapter),
                    modifier = Modifier.size(38.dp),
                )
            }
        }
    }
}

@Composable
@Preview
fun ReadToolbarPreview() {
    ReadToolbar(
        text = "Genesis 1",
        translation = "KJV",
        onSelectPassage = {},
        onSelectTranslation = {},
        onNavigateToPrevious = {},
        onNavigateToNext = {},
    )
}
