package dev.mskelton.versly.testutil

import android.content.Context
import dev.mskelton.versly.api.VerslyService
import dev.mskelton.versly.persistence.BookMetadata
import dev.mskelton.versly.persistence.Passage
import dev.mskelton.versly.persistence.PassageId
import dev.mskelton.versly.persistence.VerslyDatabase

/** Fake VerslyDatabase for testing. Provides minimal implementation needed for UI tests. */
class TestVerslyDatabase(
    private val testPassages: List<Passage>,
    context: Context =
        androidx.test.platform.app.InstrumentationRegistry
            .getInstrumentation()
            .targetContext,
) : VerslyDatabase(
        context,
        object : VerslyService {
            override suspend fun getTranslations() = throw NotImplementedError()

            override suspend fun downloadTranslation(translation: String) = throw NotImplementedError()

            override suspend fun search(
                query: String,
                translation: String,
            ) = throw NotImplementedError()
        },
    ) {
    private val bookList = mutableListOf<BookMetadata>()

    fun addBook(book: BookMetadata) {
        bookList.add(book)
    }

    override fun getBookList(translationId: String): List<BookMetadata> = bookList

    override fun getBookMetadata(
        bookId: String,
        translationId: String,
    ): BookMetadata? = bookList.firstOrNull { it.id == bookId }

    override fun getNextBook(passageId: PassageId): BookMetadata? {
        val currentIndex = bookList.indexOfFirst { it.id == passageId.book }
        return if (currentIndex >= 0 && currentIndex < bookList.size - 1) bookList[currentIndex + 1] else null
    }

    override fun getPreviousBook(passageId: PassageId): BookMetadata? {
        val currentIndex = bookList.indexOfFirst { it.id == passageId.book }
        return if (currentIndex > 0) bookList[currentIndex - 1] else null
    }

    override fun getPassage(
        book: String,
        chapter: String,
        translation: String,
        range: List<String>?,
        includeChapterNode: Boolean,
    ): Passage {
        val passage =
            testPassages.firstOrNull { p ->
                p.book == book &&
                    p.chapter == chapter &&
                    p.translation == translation &&
                    p.id.range == range
            } ?: throw IllegalArgumentException("Test passage not found: $book $chapter $translation")

        return if (!includeChapterNode) {
            passage.copy(nodes = passage.nodes.filter { it.data.getString(0) != "zc" })
        } else {
            passage
        }
    }
}
