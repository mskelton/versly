package dev.mskelton.versly.reader

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import java.io.Reader

sealed class VerseFragment {
    data class Text(val text: String) : VerseFragment()
}

sealed class ReaderNode {
    data class Book(val title: String) : ReaderNode()
    data class Chapter(val number: Number) : ReaderNode()
    data class Heading(val text: String) : ReaderNode()
    data class Verse(val number: Number, val fragments: VerseFragment) : ReaderNode()
}

@Composable
fun Reader(nodes: List<ReaderNode>) {
    Column {
        for (node in nodes) {
            when (node) {
                is ReaderNode.Book -> {
                    Text(text = node.title)
                }

                is ReaderNode.Chapter -> {
                    Text(text = "Chapter ${node.number}")
                }

                is ReaderNode.Heading -> {
                    Text(text = node.text)
                }

                is ReaderNode.Verse -> {
                    Text(text = "${node.number}. ${node.fragments}")
                    Text(
                        buildAnnotatedString {
                            withStyle(style = ParagraphStyle(lineHeight = 24.sp)) {
                                withStyle(style = SpanStyle(color = Color.Blue)) {
                                    append("Hello\n")
                                }
                                withStyle(
                                    style = SpanStyle(
                                        fontWeight = FontWeight.Bold, color = Color.Red
                                    )
                                ) {
                                    append("World\n")
                                }
                                append("Compose")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReaderPreview() {
    Reader(
        listOf(
            ReaderNode.Book("Genesis"),
            ReaderNode.Chapter(1),
            ReaderNode.Verse(
                1,
                VerseFragment.Text("In the beginning God created the heavens and the earth.")
            )
        )
    )
}
