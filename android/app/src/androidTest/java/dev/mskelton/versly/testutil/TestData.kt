package dev.mskelton.versly.testutil

import dev.mskelton.versly.persistence.Node
import dev.mskelton.versly.persistence.Passage
import dev.mskelton.versly.persistence.PassageId
import org.json.JSONArray

object TestData {
    /** Creates a chapter node (zc type) Format: ["zc", chapterTitle, chapterNumber] */
    fun createChapterNode(id: String, chapterTitle: String, chapterNumber: String): Node {
        return Node(id = id, data = JSONArray(listOf("zc", chapterTitle, chapterNumber)))
    }

    /**
     * Creates a paragraph node with text and optional verse markers Format: ["p", spansArray] Spans
     * can be strings (text) or arrays like ["v", verseNumber] for verse markers
     */
    fun createParagraphNode(id: String, spans: List<Any>): Node {
        val spansArray = JSONArray()
        spans.forEach { span ->
            when (span) {
                is String -> spansArray.put(span)
                is JSONArray -> spansArray.put(span)
                is List<*> -> spansArray.put(JSONArray(span))
                else -> spansArray.put(span)
            }
        }
        return Node(
            id = id,
            data =
                JSONArray().apply {
                    put(0, "p")
                    put(1, spansArray)
                },
        )
    }

    /** Creates a heading node (s1 type) Format: ["s1", spansArray] */
    fun createHeadingNode(id: String, text: String): Node {
        val spansArray = JSONArray().apply { put(text) }
        return Node(
            id = id,
            data =
                JSONArray().apply {
                    put(0, "s1")
                    put(1, spansArray)
                },
        )
    }

    /** Creates a verse marker span Format: ["v", verseNumber] */
    fun verseMarker(verseNumber: Int): JSONArray {
        return JSONArray(listOf("v", verseNumber.toString()))
    }

    /** Creates a PassageId */
    fun passageId(
        book: String = "GEN",
        chapter: String = "1",
        translation: String = "ESV",
        range: List<String>? = null,
    ): PassageId {
        return PassageId(book = book, chapter = chapter, translation = translation, range = range)
    }

    /** Creates a Passage with test data */
    fun passage(
        book: String = "GEN",
        chapter: String = "1",
        translation: String = "ESV",
        bookTitle: String = "Genesis",
        bookAbbreviation: String = "Gen",
        nodes: List<Node>,
        range: List<String>? = null,
    ): Passage {
        return Passage(
            id = passageId(book, chapter, translation, range),
            translation = translation,
            book = book,
            bookTitle = bookTitle,
            bookAbbreviation = bookAbbreviation,
            chapter = chapter,
            nodes = nodes,
        )
    }

    /** Creates a simple full chapter passage for testing */
    fun fullChapterPassage(
        book: String = "GEN",
        chapter: String = "1",
        translation: String = "ESV",
        bookTitle: String = "Genesis",
    ): Passage {
        val prefix = "$book.$chapter"
        val nodes =
            listOf(
                createChapterNode("$prefix.0", bookTitle, chapter),
                createParagraphNode(
                    "$prefix.1",
                    listOf(
                        verseMarker(1),
                        "In the beginning, God created the heavens and the earth. ",
                        verseMarker(2),
                        "The earth was without form and void, and darkness was over the face of the deep.",
                    ),
                ),
                createParagraphNode(
                    "$prefix.2",
                    listOf(
                        verseMarker(3),
                        "And God said, \"Let there be light,\" and there was light.",
                    ),
                ),
            )
        return passage(
            book = book,
            chapter = chapter,
            translation = translation,
            bookTitle = bookTitle,
            nodes = nodes,
        )
    }

    /** Creates a passage with partial verse range (e.g., verses 1-3) */
    fun partialRangePassage(
        book: String = "GEN",
        chapter: String = "1",
        translation: String = "ESV",
        bookTitle: String = "Genesis",
        startVerse: Int,
        endVerse: Int,
    ): Passage {
        val prefix = "$book.$chapter"
        val range = listOf(startVerse.toString(), endVerse.toString())

        // Create nodes that only include verses in the range
        val nodes = mutableListOf<Node>()
        nodes.add(createChapterNode("$prefix.0", bookTitle, chapter))

        // Add paragraph with verses in range
        val spans = mutableListOf<Any>()
        for (verse in startVerse..endVerse) {
            spans.add(verseMarker(verse))
            spans.add("Text for verse $verse. ")
        }
        nodes.add(createParagraphNode("$prefix.1", spans))

        return passage(
            book = book,
            chapter = chapter,
            translation = translation,
            bookTitle = bookTitle,
            nodes = nodes,
            range = range,
        )
    }

    /** Creates multiple passages for testing PlansScreen */
    fun multiplePassages(): List<Passage> {
        return listOf(
            fullChapterPassage("GEN", "1", "ESV", "Genesis"),
            fullChapterPassage("GEN", "2", "ESV", "Genesis"),
            partialRangePassage("MAT", "5", "ESV", "Matthew", 1, 5),
        )
    }
}
