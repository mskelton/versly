package dev.mskelton.versly

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mskelton.versly.persistence.Node
import dev.mskelton.versly.ui.theme.wordsOfJesus
import org.json.JSONArray

@Composable
fun ReaderNode(node: Node) {
    when (val type = node.data.getString(0)) {
        // Introductions
        // https://ubsicap.github.io/usfm/introductions/index.html
        "iex" -> {
            ReaderChildNode(
                node.data,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 16.dp),
                style = TextStyle(fontStyle = FontStyle.Italic),
            )
        }

        // Titles, Headings, and Labels
        // https://ubsicap.github.io/usfm/titles_headings/index.html
        "s1",
        "s2",
        "s3",
        "ms",
        -> {
            ReaderChildNode(
                node.data,
                modifier = Modifier.padding(0.dp, 32.dp, 0.dp, 24.dp),
                style = TextStyle(fontWeight = FontWeight.Bold),
                fontSize = 20.sp,
                lineHeight = 28.sp,
            )
        }

        "d" -> {
            ReaderChildNode(
                node.data,
                modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 24.dp),
                style = TextStyle(fontStyle = FontStyle.Italic),
            )
        }

        "sp" -> {
            Text(
                text = node.data.getString(1),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(0.dp, 16.dp, 0.dp, 24.dp),
                style = TextStyle(fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold),
                fontSize = 18.sp,
                lineHeight = 36.sp,
            )
        }

        // Paragraphs
        // https://ubsicap.github.io/usfm/paragraphs/index.html
        "p" -> {
            ReaderChildNode(
                node.data,
                modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 16.dp),
                style = TextStyle(textIndent = TextIndent(8.sp)),
            )
        }

        "m" -> {
            ReaderChildNode(node.data, modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 16.dp))
        }

        "pr",
        "cls",
        -> {
            ReaderChildNode(
                node.data,
                modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 16.dp),
                //                    .align(Alignment.End),
            )
        }

        "pmo",
        "pmc",
        -> {
            ReaderChildNode(node.data, modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 16.dp))
        }

        "pm" -> {
            ReaderChildNode(
                node.data,
                modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 16.dp),
                style = TextStyle(textIndent = TextIndent(8.sp)),
            )
        }

        "pmr" -> {
            ReaderChildNode(
                node.data,
                modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 16.dp),
                //                    .align(Alignment.End),
            )
        }

        "pi1" -> {
            ReaderChildNode(
                node.data,
                modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 16.dp),
                style = TextStyle(textIndent = TextIndent(8.sp)),
            )
        }

        "pi2" -> {
            ReaderChildNode(
                node.data,
                modifier = Modifier.padding(32.dp, 0.dp, 0.dp, 16.dp),
                style = TextStyle(textIndent = TextIndent(8.sp)),
            )
        }

        "pi3" -> {
            ReaderChildNode(
                node.data,
                modifier = Modifier.padding(48.dp, 0.dp, 0.dp, 16.dp),
                style = TextStyle(textIndent = TextIndent(8.sp)),
            )
        }

        "mi" -> {
            ReaderChildNode(node.data, modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 16.dp))
        }

        "nb" -> {
            ReaderChildNode(node.data, style = TextStyle(textIndent = TextIndent(8.sp)))
        }

        "pc" -> {
            Column(modifier = Modifier.fillMaxWidth()) {
                ReaderChildNode(node.data, modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 16.dp))
            }
        }

        "b" -> {
            Box(modifier = Modifier.height(16.dp))
        }

        // Poetry
        // https://ubsicap.github.io/usfm/poetry/index.html
        "q1" -> {
            ReaderChildNode(
                node.data,
                modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 16.dp),
                style = TextStyle(textIndent = TextIndent(restLine = 48.sp)),
            )
        }

        "q2" -> {
            ReaderChildNode(
                node.data,
                modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 16.dp),
                style = TextStyle(textIndent = TextIndent(restLine = 48.sp)),
            )
        }

        "q3" -> {
            ReaderChildNode(
                node.data,
                modifier = Modifier.padding(32.dp, 0.dp, 0.dp, 16.dp),
                style = TextStyle(textIndent = TextIndent(restLine = 48.sp)),
            )
        }

        "q4" -> {
            ReaderChildNode(
                node.data,
                modifier = Modifier.padding(48.dp, 0.dp, 0.dp, 16.dp),
                style = TextStyle(textIndent = TextIndent(restLine = 48.sp)),
            )
        }

        "qr" -> {
            ReaderChildNode(
                node.data,
                modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 16.dp),
                //                    .align(Alignment.End),
            )
        }

        "qc" -> {
            ReaderChildNode(
                node.data,
                modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 16.dp),
                //                        .align(Alignment.CenterHorizontally),
            )
        }

        "qa" -> {
            ReaderChildNode(
                node.data,
                modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 16.dp),
                //                        .align(Alignment.CenterHorizontally),
                style = TextStyle(fontStyle = FontStyle.Italic),
            )
        }

        "qm1" -> {
            ReaderChildNode(
                node.data,
                modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 16.dp),
                style = TextStyle(textIndent = TextIndent(restLine = 16.sp)),
            )
        }

        "qm2" -> {
            ReaderChildNode(
                node.data,
                modifier = Modifier.padding(32.dp, 0.dp, 0.dp, 16.dp),
                style = TextStyle(textIndent = TextIndent(restLine = 16.sp)),
            )
        }

        // Lists
        // https://ubsicap.github.io/usfm/lists/index.html
        "li1",
        "lim",
        -> {
            ReaderChildNode(node.data, modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 8.dp))
        }

        "li2" -> {
            ReaderChildNode(node.data, modifier = Modifier.padding(32.dp, 0.dp, 0.dp, 8.dp))
        }

        "li3" -> {
            ReaderChildNode(node.data, modifier = Modifier.padding(48.dp, 0.dp, 0.dp, 8.dp))
        }

        "li4" -> {
            ReaderChildNode(node.data, modifier = Modifier.padding(64.dp, 0.dp, 0.dp, 8.dp))
        }

        // Tables
        // https://ubsicap.github.io/usfm/tables/index.html
        "table" -> {
            ReaderTable(node.data)
        }

        // Custom nodes
        // https://ubsicap.github.io/usfm/about/syntax.html?highlight=zmy#z-namespace
        "zc" -> {
            ChapterNode(node.data)
        }

        else -> {
            error("Unknown node type: $type")
        }
    }
}

@Composable
fun ReaderTable(node: JSONArray) {
    val rows = node.getJSONArray(1)
    val totalColumns = (0 until rows.length()).maxOfOrNull { rows.getJSONArray(it).length() } ?: 1

    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        for (i in 0 until rows.length()) {
            val row = rows.getJSONArray(i)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (j in 0 until totalColumns) {
                    Box(modifier = Modifier.weight(1f).padding(4.dp)) {
                        if (j < row.length()) {
                            val cell = row.getJSONArray(j)
                            val isHeader = cell.getString(0) == "th"

                            ReaderChildNode(
                                node = cell,
                                style =
                                    TextStyle(
                                        fontWeight =
                                            if (isHeader) {
                                                FontWeight.Bold
                                            } else {
                                                FontWeight.Normal
                                            },
                                    ),
                                lineHeight = 24.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChapterNode(node: JSONArray) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 40.dp, bottom = 32.dp)) {
        Text(
            text = node.getString(1),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
        )
        Text(
            text = node.getString(2),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.Bold,
            fontSize = 72.sp,
        )
    }
}

@Composable
fun ReaderChildNode(
    node: JSONArray,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    style: TextStyle = TextStyle(),
    fontSize: TextUnit = 18.sp,
    lineHeight: TextUnit = 36.sp,
) {
    val spans = node.getJSONArray(1)

    Text(
        text =
            buildAnnotatedString {
                for (j in 0 until spans.length()) {
                    val span = spans.get(j)

                    // Simple text strings
                    if (span is String) {
                        append(span)
                        continue
                    }

                    val childNode = spans.getJSONArray(j)
                    when (val type = childNode.getString(0)) {
                        "v" -> {
                            withStyle(
                                style =
                                    SpanStyle(
                                        baselineShift = BaselineShift(0.3f),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp,
                                    ),
                            ) {
                                append(childNode.getString(1) + "\u00A0")
                            }
                        }

                        "qs" -> {
                            withStyle(style = ParagraphStyle(textAlign = TextAlign.End)) {
                                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                                    append(childNode.getString(1))
                                }
                            }
                        }

                        "qac" -> {
                            withStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                ),
                            ) {
                                append(childNode.getString(1))
                            }
                        }

                        "litl" -> {
                            withStyle(style = ParagraphStyle(textAlign = TextAlign.End)) {
                                append(childNode.getString(1))
                            }
                        }

                        "wj" -> {
                            withStyle(
                                style = SpanStyle(color = MaterialTheme.colorScheme.wordsOfJesus),
                            ) {
                                append(childNode.getString(1))
                            }
                        }

                        "em",
                        "bd",
                        -> {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(childNode.getString(1))
                            }
                        }

                        "bk",
                        "qt",
                        "sig",
                        "sls",
                        "tl",
                        "it",
                        -> {
                            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                append(childNode.getString(1))
                            }
                        }

                        "nd",
                        "sc",
                        -> {
                            withStyle(SpanStyle(fontFeatureSettings = "smcp")) {
                                append(childNode.getString(1))
                            }
                        }

                        "sup" -> {
                            withStyle(
                                SpanStyle(
                                    baselineShift = BaselineShift.Superscript,
                                    fontSize = 12.sp,
                                ),
                            ) {
                                append(childNode.getString(1))
                            }
                        }

                        "no" -> {
                            withStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.Normal,
                                    fontStyle = FontStyle.Normal,
                                ),
                            ) {
                                append(childNode.getString(1))
                            }
                        }

                        "t" -> {
                            append(childNode.getString(1))
                        }

                        else -> {
                            error("Unknown node type: $type")
                        }
                    }
                }
            },
        modifier = modifier,
        color = color,
        style = style,
        fontSize = fontSize,
        lineHeight = lineHeight,
    )
}
