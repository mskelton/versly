package dev.mskelton.versly.persistence

import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BibleFilterTest {
    // region helpers

    private fun verse(num: Int) =
        JSONArray().apply {
            put("v")
            put(num.toString())
        }

    private fun span(type: String) = JSONArray().apply { put(type) }

    private fun contentNode(
        type: String,
        vararg spans: Any,
    ) = JSONArray().apply {
        put(type)
        put(JSONArray().apply { spans.forEach { put(it) } })
    }

    private fun structuralNode(type: String) = JSONArray().apply { put(type) }

    private fun chapterData(vararg nodes: JSONArray) =
        JSONArray().apply {
            nodes.forEach { put(it) }
        }

    private fun Node.type() = data.getString(0)

    private fun Node.spans() = data.getJSONArray(1)

    private fun Node.spanCount() = spans().length()

    private fun Node.spanAt(i: Int): Any = spans().get(i)

    private fun Node.spanTypeAt(i: Int) = spans().getJSONArray(i).getString(0)

    // endregion

    // region filterNodesByRange

    @Test
    fun emptyRange_returnsEmptyList() {
        val data = chapterData(contentNode("p", verse(1), "text"))
        assertTrue(filterNodesByRange(data, emptyList(), "GEN.1").isEmpty())
    }

    @Test
    fun nonNumericRange_returnsEmptyList() {
        val data = chapterData(contentNode("p", verse(1), "text"))
        assertTrue(filterNodesByRange(data, listOf("abc", "def"), "GEN.1").isEmpty())
    }

    @Test
    fun singleElementRange_treatedAsSingleVerse() {
        val data =
            chapterData(
                contentNode("p", verse(1), "v1 ", verse(2), "v2"),
            )
        val nodes = filterNodesByRange(data, listOf("2"), "GEN.1")
        assertEquals(1, nodes.size)
        assertEquals("v", nodes[0].spanTypeAt(0))
        assertEquals("2", nodes[0].spans().getJSONArray(0).getString(1))
        assertEquals("v2", nodes[0].spanAt(1))
    }

    @Test
    fun singleVerseRange_returnsOnlyThatVerse() {
        val data =
            chapterData(
                contentNode("p", verse(1), "v1 ", verse(2), "v2 ", verse(3), "v3"),
            )
        val nodes = filterNodesByRange(data, listOf("2", "2"), "GEN.1")
        assertEquals(1, nodes.size)
        assertEquals(2, nodes[0].spanCount())
        assertEquals("v", nodes[0].spanTypeAt(0))
        assertEquals("2", nodes[0].spans().getJSONArray(0).getString(1))
        assertEquals("v2 ", nodes[0].spanAt(1))
    }

    @Test
    fun multiVerseRange_returnsNodesInRange() {
        val data =
            chapterData(
                contentNode("p", verse(1), "v1 ", verse(2), "v2 ", verse(3), "v3"),
            )
        val nodes = filterNodesByRange(data, listOf("2", "3"), "GEN.1")
        assertEquals(1, nodes.size)
        assertEquals(4, nodes[0].spanCount()) // v2 marker, v2 text, v3 marker, v3 text
    }

    @Test
    fun nodeEntirelyBeforeRange_excluded() {
        val data =
            chapterData(
                contentNode("p", verse(1), "v1"),
                contentNode("p", verse(2), "v2"),
            )
        val nodes = filterNodesByRange(data, listOf("2", "2"), "GEN.1")
        assertEquals(1, nodes.size)
        assertEquals("GEN.1.2", nodes[0].id)
    }

    @Test
    fun nodeEntirelyAfterRange_excluded() {
        val data =
            chapterData(
                contentNode("p", verse(1), "v1"),
                contentNode("p", verse(2), "v2"),
            )
        val nodes = filterNodesByRange(data, listOf("1", "1"), "GEN.1")
        assertEquals(1, nodes.size)
        assertEquals("GEN.1.1", nodes[0].id)
    }

    @Test
    fun nodeIdUsesOneBasedPositionWithPrefix() {
        val data =
            chapterData(
                contentNode("p", verse(1), "v1"),
                contentNode("p", verse(2), "v2"),
                contentNode("p", verse(3), "v3"),
            )
        val nodes = filterNodesByRange(data, listOf("2", "3"), "REV.22")
        assertEquals(2, nodes.size)
        assertEquals("REV.22.2", nodes[0].id)
        assertEquals("REV.22.3", nodes[1].id)
    }

    @Test
    fun filteredNode_preservesNodeType() {
        val data =
            chapterData(
                contentNode("q1", verse(1), "poetry"),
            )
        val nodes = filterNodesByRange(data, listOf("1", "1"), "PSA.23")
        assertEquals(1, nodes.size)
        assertEquals("q1", nodes[0].type())
    }

    @Test
    fun versesAcrossMultipleNodes_eachNodeFilteredIndependently() {
        val data =
            chapterData(
                contentNode("p", verse(1), "v1 ", verse(2), "v2"),
                contentNode("p", verse(3), "v3 ", verse(4), "v4"),
            )
        val nodes = filterNodesByRange(data, listOf("2", "3"), "GEN.1")
        assertEquals(2, nodes.size)
        // First node: only verse 2
        assertEquals(2, nodes[0].spanCount())
        assertEquals("2", nodes[0].spans().getJSONArray(0).getString(1))
        // Second node: only verse 3
        assertEquals(2, nodes[1].spanCount())
        assertEquals("3", nodes[1].spans().getJSONArray(0).getString(1))
    }

    @Test
    fun outOfRangeData_returnsEmptyList() {
        val data =
            chapterData(
                contentNode("p", verse(1), "v1"),
                contentNode("p", verse(2), "v2"),
            )
        assertTrue(filterNodesByRange(data, listOf("10", "12"), "GEN.1").isEmpty())
    }

    // endregion

    // region structural nodes

    @Test
    fun structuralNode_includedWhenNextVerseInRange() {
        val data =
            chapterData(
                structuralNode("s1"),
                contentNode("p", verse(3), "v3"),
            )
        val nodes = filterNodesByRange(data, listOf("3", "3"), "GEN.1")
        assertEquals(2, nodes.size)
        assertEquals("s1", nodes[0].type())
    }

    @Test
    fun structuralNode_excludedWhenNextVerseOutOfRange() {
        val data =
            chapterData(
                structuralNode("s1"),
                contentNode("p", verse(3), "v3"),
            )
        val nodes = filterNodesByRange(data, listOf("1", "2"), "GEN.1")
        assertTrue(nodes.isEmpty())
    }

    @Test
    fun allStructuralNodeTypes_recognizedAsPureStructural() {
        for (type in listOf("s1", "s2", "s3", "ms", "sp", "d", "iex")) {
            val data =
                chapterData(
                    structuralNode(type),
                    contentNode("p", verse(1), "text"),
                )
            val nodes = filterNodesByRange(data, listOf("1", "1"), "GEN.1")
            assertEquals("$type should be treated as structural", 2, nodes.size)
            assertEquals(type, nodes[0].type())
        }
    }

    @Test
    fun structuralNode_atEndOfChapterWithNoNextVerse_excluded() {
        val data =
            chapterData(
                contentNode("p", verse(1), "v1"),
                structuralNode("s1"),
            )
        val nodes = filterNodesByRange(data, listOf("2", "2"), "GEN.1")
        assertTrue(nodes.isEmpty())
    }

    // endregion

    // region non-verse spans

    @Test
    fun nonVerseSpan_includedWhenCurrentVerseInRange() {
        val italicSpan = span("it")
        val data =
            chapterData(
                contentNode("p", verse(1), italicSpan, "italic text"),
            )
        val nodes = filterNodesByRange(data, listOf("1", "1"), "GEN.1")
        assertEquals(1, nodes.size)
        assertEquals(3, nodes[0].spanCount()) // verse marker, italic span, text
    }

    @Test
    fun nonVerseSpan_excludedWhenCurrentVerseOutOfRange() {
        val italicSpan = span("it")
        val data =
            chapterData(
                contentNode("p", verse(1), italicSpan, "text", verse(2), "v2"),
            )
        val nodes = filterNodesByRange(data, listOf("2", "2"), "GEN.1")
        assertEquals(1, nodes.size)
        assertEquals(2, nodes[0].spanCount()) // only verse 2 marker + text
    }

    // endregion

    // region filterSpansByRange

    @Test
    fun filterSpansByRange_emptySpans_returnsEmptyAndNull() {
        val (spans, lastVerse) = filterSpansByRange(JSONArray(), 1, 3)
        assertTrue(spans.isEmpty())
        assertEquals(null, lastVerse)
    }

    @Test
    fun filterSpansByRange_singleVerse_returnsMarkerAndText() {
        val spans =
            JSONArray().apply {
                put(verse(2))
                put("hello")
            }
        val (filtered, lastVerse) = filterSpansByRange(spans, 2, 2)
        assertEquals(2, filtered.size)
        assertEquals(2, lastVerse)
    }

    @Test
    fun filterSpansByRange_versePastEnd_stops() {
        val spans =
            JSONArray().apply {
                put(verse(1))
                put("v1")
                put(verse(2))
                put("v2")
                put(verse(3))
                put("v3")
            }
        val (filtered, lastVerse) = filterSpansByRange(spans, 1, 2)
        assertEquals(4, filtered.size) // v1 marker, v1 text, v2 marker, v2 text
        assertEquals(2, lastVerse)
    }

    @Test
    fun filterSpansByRange_initialVerse_usedForTextBeforeFirstMarker() {
        val spans =
            JSONArray().apply {
                put("preamble")
                put(verse(2))
                put("v2")
            }
        val (filtered, lastVerse) = filterSpansByRange(spans, 1, 1, initialVerse = 1)
        assertEquals(1, filtered.size) // only "preamble" since verse 2 is out of range
        assertEquals("preamble", filtered[0])
        assertEquals(1, lastVerse)
    }

    // endregion
}
