package dev.mskelton.versly.testutil

import android.content.Context
import dev.mskelton.versly.api.VerslyService
import dev.mskelton.versly.persistence.BibleDatabase
import dev.mskelton.versly.persistence.BookMetadata
import dev.mskelton.versly.persistence.Passage

/** Fake BibleDatabase for testing Provides minimal implementation needed for UI tests */
class TestBibleDatabase(
    private val testPassages: List<Passage>,
    context: Context =
        androidx.test.platform.app.InstrumentationRegistry
            .getInstrumentation()
            .targetContext,
) : BibleDatabase(
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

    override fun getPassage(
        book: String,
        chapter: String,
        translation: String,
        range: List<String>?,
    ): Passage =
        testPassages.firstOrNull { passage ->
            passage.book == book &&
                passage.chapter == chapter &&
                passage.translation == translation &&
                passage.id.range == range
        } ?: throw IllegalArgumentException("Test passage not found: $book $chapter $translation")
}
