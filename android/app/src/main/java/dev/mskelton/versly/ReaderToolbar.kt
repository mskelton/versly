package dev.mskelton.versly

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

const val ICON_SIZE = 32

@Composable
fun ReaderToolbar(
    modifier: Modifier = Modifier,
    text: String,
    translation: String,
    onSelectPassage: () -> Unit,
    onSelectTranslation: () -> Unit,
    onNavigateToPrevious: () -> Unit,
    onNavigateToNext: () -> Unit,
) {
    Box(modifier = modifier) {
        val surfaceColor = MaterialTheme.colorScheme.surface
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    surfaceColor.copy(alpha = 0f),
                                    surfaceColor,
                                ),
                        ),
                    ),
        )

        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(12.dp)
                    .align(Alignment.BottomCenter),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shape = RoundedCornerShape(32.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(32.dp),
                    onClick = onNavigateToPrevious,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.chevron_left_24px),
                        contentDescription = stringResource(R.string.previous_chapter),
                        modifier = Modifier.size(ICON_SIZE.dp),
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxHeight().weight(1f),
                    onClick = onSelectPassage,
                ) {
                    Surface(modifier = Modifier.padding(horizontal = 8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Left,
                            )

                            Surface(
                                shape = RoundedCornerShape(32.dp),
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                modifier = Modifier.padding(2.dp),
                                onClick = onSelectTranslation,
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
                                ) {
                                    Text(
                                        text = translation,
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                }
                            }
                        }
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(32.dp),
                    onClick = onNavigateToNext,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.chevron_right_24px),
                        contentDescription = stringResource(R.string.next_chapter),
                        modifier = Modifier.size(ICON_SIZE.dp),
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun ReadToolbarPreview() {
    ReaderToolbar(
        text = "Genesis 1",
        translation = "KJV",
        onSelectPassage = {},
        onSelectTranslation = {},
        onNavigateToPrevious = {},
        onNavigateToNext = {},
    )
}
