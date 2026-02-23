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

    // region Passage.format

    @Test
    fun format_fullChapter_returnsBookAndChapter() {
        val passage = fullChapter("ROM", "Romans", "4")
        assertEquals("Romans 4", passage.format())
    }

    @Test
    fun format_partialChapterWithRange_returnsBookChapterAndRange() {
        val passage = partialChapter("ROM", "Romans", "5", 1, 11)
        assertEquals("Romans 5:1-11", passage.format())
    }

    @Test
    fun format_partialChapterSingleVerse_returnsBookChapterAndVerse() {
        val passage = partialChapter("ROM", "Romans", "5", 3, 3)
        assertEquals("Romans 5:3", passage.format())
    }

    // endregion
}
