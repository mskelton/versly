package dev.mskelton.versly

import dev.mskelton.versly.persistence.Passage
import dev.mskelton.versly.persistence.PassageId
import org.junit.Assert.assertEquals
import org.junit.Test

class PlansScreenTest {
    // region helpers

    private fun fullChapter(
        book: String,
        bookTitle: String,
        chapter: String,
    ) = Passage(
        id = PassageId(book = book, chapter = chapter, translation = "ESV"),
        book = book,
        bookTitle = bookTitle,
        bookAbbreviation = bookTitle.take(3),
        chapter = chapter,
        translation = "ESV",
        nodes = emptyList(),
    )

    private fun partialChapter(
        book: String,
        bookTitle: String,
        chapter: String,
        startVerse: Int,
        endVerse: Int,
    ) = Passage(
        id =
            PassageId(
                book = book,
                chapter = chapter,
                translation = "ESV",
                range = listOf(startVerse.toString(), endVerse.toString()),
            ),
        book = book,
        bookTitle = bookTitle,
        bookAbbreviation = bookTitle.take(3),
        chapter = chapter,
        translation = "ESV",
        nodes = emptyList(),
    )

    // endregion

    // region groupPassages

    @Test
    fun groupPassages_singlePassage_returnsSingleGroup() {
        val passages = listOf(fullChapter("ROM", "Romans", "4"))
        val groups = groupPassages(passages)
        assertEquals(1, groups.size)
        assertEquals(1, groups[0].passages.size)
    }

    @Test
    fun groupPassages_consecutiveFullChapters_mergesIntoOneGroup() {
        val passages =
            listOf(
                fullChapter("ROM", "Romans", "4"),
                fullChapter("ROM", "Romans", "5"),
            )
        val groups = groupPassages(passages)
        assertEquals(1, groups.size)
        assertEquals(2, groups[0].passages.size)
    }

    @Test
    fun groupPassages_fullChapterFollowedByPartialChapter_mergesIntoOneGroup() {
        val passages =
            listOf(
                fullChapter("ROM", "Romans", "4"),
                partialChapter("ROM", "Romans", "5", 1, 11),
            )
        val groups = groupPassages(passages)
        assertEquals(1, groups.size)
        assertEquals(2, groups[0].passages.size)
    }

    @Test
    fun groupPassages_partialChapterFollowedByFullChapter_doesNotMerge() {
        val passages =
            listOf(
                partialChapter("ROM", "Romans", "4", 1, 11),
                fullChapter("ROM", "Romans", "5"),
            )
        val groups = groupPassages(passages)
        assertEquals(2, groups.size)
    }

    @Test
    fun groupPassages_partialChapterFollowedByPartialChapter_doesNotMerge() {
        val passages =
            listOf(
                partialChapter("ROM", "Romans", "4", 1, 10),
                partialChapter("ROM", "Romans", "5", 1, 11),
            )
        val groups = groupPassages(passages)
        assertEquals(2, groups.size)
    }

    @Test
    fun groupPassages_differentBooks_doesNotMerge() {
        val passages =
            listOf(
                fullChapter("ROM", "Romans", "4"),
                fullChapter("GAL", "Galatians", "1"),
            )
        val groups = groupPassages(passages)
        assertEquals(2, groups.size)
    }

    @Test
    fun groupPassages_nonConsecutiveChapters_doesNotMerge() {
        val passages =
            listOf(
                fullChapter("ROM", "Romans", "4"),
                fullChapter("ROM", "Romans", "6"),
            )
        val groups = groupPassages(passages)
        assertEquals(2, groups.size)
    }

    @Test
    fun groupPassages_fullChapterThenPartialThenFull_partialBreaksChain() {
        val passages =
            listOf(
                fullChapter("ROM", "Romans", "4"),
                partialChapter("ROM", "Romans", "5", 1, 11),
                fullChapter("ROM", "Romans", "6"),
            )
        val groups = groupPassages(passages)
        assertEquals(2, groups.size)
        assertEquals(2, groups[0].passages.size) // Romans 4 + 5:1-11
        assertEquals(1, groups[1].passages.size) // Romans 6
    }

    // endregion

    // region PassageGroup.format

    @Test
    fun format_singleFullChapter_returnsBookAndChapter() {
        val group = PassageGroup(listOf(fullChapter("ROM", "Romans", "4")))
        assertEquals("Romans 4", group.format())
    }

    @Test
    fun format_singlePartialChapter_returnsBookChapterAndRange() {
        val group = PassageGroup(listOf(partialChapter("ROM", "Romans", "5", 1, 11)))
        assertEquals("Romans 5:1-11", group.format())
    }

    @Test
    fun format_multipleFullChapters_returnsChapterRange() {
        val group =
            PassageGroup(
                listOf(
                    fullChapter("ROM", "Romans", "4"),
                    fullChapter("ROM", "Romans", "5"),
                    fullChapter("ROM", "Romans", "6"),
                ),
            )
        assertEquals("Romans 4-6", group.format())
    }

    @Test
    fun format_fullChapterFollowedByPartialChapter_includesEndVerse() {
        val group =
            PassageGroup(
                listOf(
                    fullChapter("ROM", "Romans", "4"),
                    partialChapter("ROM", "Romans", "5", 1, 11),
                ),
            )
        assertEquals("Romans 4-5:11", group.format())
    }

    @Test
    fun format_multipleFullChaptersThenPartial_includesEndVerse() {
        val group =
            PassageGroup(
                listOf(
                    fullChapter("ROM", "Romans", "3"),
                    fullChapter("ROM", "Romans", "4"),
                    partialChapter("ROM", "Romans", "5", 1, 11),
                ),
            )
        assertEquals("Romans 3-5:11", group.format())
    }

    // endregion
}
