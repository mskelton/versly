package dev.mskelton.versly

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.mskelton.versly.persistence.MemoryVerse
import dev.mskelton.versly.persistence.MemoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    verse: MemoryVerse,
    viewModel: MemoryViewModel,
) {
    val backStack = LocalBackStack.current
    var revealed by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.practice)) },
                navigationIcon = {
                    IconButton(onClick = { backStack.removeLastOrNull() }) {
                        Icon(
                            painter = painterResource(R.drawable.chevron_left_24px),
                            contentDescription = stringResource(R.string.navigate_up),
                        )
                    }
                },
                windowInsets = WindowInsets(),
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MasteryBadge(level = verse.masteryLevel)

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = 3.dp,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = verse.reference,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )

                    AnimatedVisibility(
                        visible = revealed,
                        enter = fadeIn() + slideInVertically { it / 2 },
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = verse.translation,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = verse.text,
                                style = MaterialTheme.typography.bodyLarge,
                                fontStyle = FontStyle.Italic,
                                textAlign = TextAlign.Center,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (!revealed) {
                Button(
                    onClick = { revealed = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.reveal))
                }
            } else {
                Text(
                    text = "How well did you know it?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            val newLevel = maxOf(0, verse.masteryLevel - 1)
                            viewModel.updateMastery(verse.id, newLevel)
                            backStack.removeLastOrNull()
                        },
                        modifier = Modifier.weight(1f),
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                            ),
                    ) {
                        Text(stringResource(R.string.again))
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.updateMastery(verse.id, verse.masteryLevel)
                            backStack.removeLastOrNull()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.almost))
                    }

                    Button(
                        onClick = {
                            val newLevel = minOf(3, verse.masteryLevel + 1)
                            viewModel.updateMastery(verse.id, newLevel)
                            backStack.removeLastOrNull()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.got_it))
                    }
                }

                Box(modifier = Modifier.height(16.dp))

                CurrentMasteryInfo(level = verse.masteryLevel)
            }
        }
    }
}

@Composable
private fun CurrentMasteryInfo(level: Int) {
    val nextLevel = minOf(3, level + 1)
    val nextLabel =
        when (nextLevel) {
            0 -> stringResource(R.string.mastery_new)
            1 -> stringResource(R.string.mastery_learning)
            2 -> stringResource(R.string.mastery_familiar)
            else -> stringResource(R.string.mastery_mastered)
        }
    val currentLabel =
        when (level) {
            0 -> stringResource(R.string.mastery_new)
            1 -> stringResource(R.string.mastery_learning)
            2 -> stringResource(R.string.mastery_familiar)
            else -> stringResource(R.string.mastery_mastered)
        }

    if (level < 3) {
        Text(
            text = "Current: $currentLabel → Next: $nextLabel",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        Text(
            text = "You've mastered this verse!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
