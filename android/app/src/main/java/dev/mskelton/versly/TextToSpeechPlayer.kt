package dev.mskelton.versly

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun TextToSpeechPlayer(
    text: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var isRepeat by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    val tts =
        remember {
            var instance: TextToSpeech? = null
            instance =
                TextToSpeech(context) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        instance?.language = Locale.US
                        instance?.setSpeechRate(0.85f)
                    }
                }
            instance
        }

    var sentenceOffsets by remember { mutableStateOf(listOf<Int>()) }

    fun queueSentences() {
        val sentences = text.split(Regex("(?<=[.!?])\\s+")).filter { it.isNotEmpty() }
        var offset = 0
        sentenceOffsets =
            sentences.map { sentence ->
                val cur = offset
                offset += sentence.length + 1
                cur
            }
        sentences.forEachIndexed { index, sentence ->
            val queueMode =
                if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            tts.speak(sentence, queueMode, null, "verse_$index")
            if (index < sentences.size - 1) {
                tts.playSilentUtterance(200, TextToSpeech.QUEUE_ADD, "silence_$index")
            }
        }
    }

    DisposableEffect(tts) {
        tts.setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    if (utteranceId?.startsWith("verse_") == true) {
                        isPlaying = true
                    }
                }

                override fun onDone(utteranceId: String?) {
                    if (utteranceId?.startsWith("silence") == true) return
                    val index =
                        utteranceId?.removePrefix("verse_")?.toIntOrNull() ?: return
                    if (index == sentenceOffsets.size - 1) {
                        progress = 0f
                        if (isRepeat) {
                            queueSentences()
                        } else {
                            isPlaying = false
                        }
                    }
                }

                @Suppress("OVERRIDE_DEPRECATION")
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    isPlaying = false
                    progress = 0f
                }

                override fun onError(
                    utteranceId: String?,
                    errorCode: Int,
                ) {
                    isPlaying = false
                    progress = 0f
                }

                override fun onRangeStart(
                    utteranceId: String?,
                    start: Int,
                    end: Int,
                    frame: Int,
                ) {
                    val sentenceIndex =
                        utteranceId?.removePrefix("verse_")?.toIntOrNull() ?: return
                    if (sentenceIndex < sentenceOffsets.size && text.isNotEmpty()) {
                        val absoluteEnd = sentenceOffsets[sentenceIndex] + end
                        progress = absoluteEnd.toFloat() / text.length.toFloat()
                    }
                }
            },
        )

        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    fun play() {
        progress = 0f
        queueSentences()
    }

    fun pause() {
        tts.stop()
        isPlaying = false
    }

    fun stop() {
        tts.stop()
        isPlaying = false
        isExpanded = false
        isRepeat = false
        progress = 0f
    }

    AnimatedContent(
        targetState = isExpanded,
        modifier = modifier,
        transitionSpec = {
            fadeIn() togetherWith fadeOut() using SizeTransform(clip = false)
        },
        contentAlignment = Alignment.BottomEnd,
        label = "tts_toolbar",
    ) { expanded ->
        if (!expanded) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.BottomEnd,
            ) {
                FloatingActionButton(
                    onClick = {
                        isExpanded = true
                        play()
                    },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.volume_up_24px),
                        contentDescription = stringResource(R.string.listen_to_verse),
                    )
                }
            }
        } else {
            Box {
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
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val animatedProgress by animateFloatAsState(
                            targetValue = progress,
                            label = "progress",
                        )

                        IconButton(onClick = { if (isPlaying) pause() else play() }) {
                            Icon(
                                painter =
                                    painterResource(
                                        if (isPlaying) R.drawable.pause_24px else R.drawable.play_arrow_24px,
                                    ),
                                contentDescription =
                                    if (isPlaying) {
                                        stringResource(R.string.pause)
                                    } else {
                                        stringResource(R.string.play)
                                    },
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.size(24.dp).padding(end = 4.dp),
                            strokeWidth = 3.dp,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )

                        IconButton(
                            onClick = { isRepeat = !isRepeat },
                            colors =
                                if (isRepeat) {
                                    IconButtonDefaults.filledTonalIconButtonColors()
                                } else {
                                    IconButtonDefaults.iconButtonColors()
                                },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.repeat_24px),
                                contentDescription = stringResource(R.string.repeat),
                            )
                        }

                        IconButton(onClick = { stop() }) {
                            Icon(
                                painter = painterResource(R.drawable.close_24px),
                                contentDescription = stringResource(R.string.stop),
                            )
                        }
                    }
                }
            }
        }
    }
}
