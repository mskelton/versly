package dev.mskelton.versly.ui

import dev.mskelton.versly.testutil.TestData
import dev.mskelton.versly.testutil.TestVerslyDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GetPassageTest {
    @Test
    fun getPassage_includeChapterNode_true_includesZcNode() {
        val passage = TestData.fullChapterPassage()
        val db = TestVerslyDatabase(listOf(passage))

        val result = db.getPassage("GEN", "1", "ESV", includeChapterNode = true)

        assertTrue(result.nodes.any { it.data.getString(0) == "zc" })
    }

    @Test
    fun getPassage_includeChapterNode_false_excludesZcNode() {
        val passage = TestData.fullChapterPassage()
        val db = TestVerslyDatabase(listOf(passage))

        val result = db.getPassage("GEN", "1", "ESV", includeChapterNode = false)

        assertFalse(result.nodes.any { it.data.getString(0) == "zc" })
    }

    @Test
    fun getPassage_includeChapterNode_false_preservesOtherNodes() {
        val passage = TestData.fullChapterPassage()
        val db = TestVerslyDatabase(listOf(passage))

        val withChapter = db.getPassage("GEN", "1", "ESV", includeChapterNode = true)
        val withoutChapter = db.getPassage("GEN", "1", "ESV", includeChapterNode = false)

        val nonZcCount = withChapter.nodes.count { it.data.getString(0) != "zc" }
        assertEquals(nonZcCount, withoutChapter.nodes.size)
    }

    @Test
    fun getPassage_includeChapterNode_defaultsToTrue() {
        val passage = TestData.fullChapterPassage()
        val db = TestVerslyDatabase(listOf(passage))

        val result = db.getPassage("GEN", "1", "ESV")

        assertTrue(result.nodes.any { it.data.getString(0) == "zc" })
    }

    @Test
    fun getPassage_withRange_includeChapterNode_false() {
        val passage = TestData.partialRangePassage(
            book = "GEN",
            chapter = "1",
            startVerse = 1,
            endVerse = 3,
        )
        val db = TestVerslyDatabase(listOf(passage))

        val result = db.getPassage("GEN", "1", "ESV", listOf("1", "3"), includeChapterNode = false)

        assertFalse(result.nodes.any { it.data.getString(0) == "zc" })
        assertTrue(result.nodes.isNotEmpty())
    }
}
