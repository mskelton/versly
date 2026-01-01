package dev.mskelton.versly.testutil

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import dev.mskelton.versly.Read
import dev.mskelton.versly.api.VerslyService
import dev.mskelton.versly.persistence.BibleDatabase
import dev.mskelton.versly.persistence.Passage
import dev.mskelton.versly.persistence.PassageId
import dev.mskelton.versly.persistence.ReadViewModel

/**
 * Test ReadViewModel that uses a fake BibleDatabase with test data
 */
class TestReadViewModel(
    testPassages: List<Passage>,
    context: Context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext,
) : ReadViewModel(
    bibleDatabase = TestBibleDatabase(testPassages, context),
    savedStateHandle = SavedStateHandle(),
    navKey = Read(),
)

/**
 * Test BibleDatabase that returns test passages
 */
private class TestBibleDatabase(
    private val testPassages: List<Passage>,
    context: Context,
) : BibleDatabase(
    context,
    object : VerslyService {
        override suspend fun getTranslations() = throw NotImplementedError()
        override suspend fun downloadTranslation(translation: String) = throw NotImplementedError()
        override suspend fun search(query: String, translation: String) = throw NotImplementedError()
    },
) {
    override fun getPassage(
        book: String,
        chapter: String,
        translation: String,
        range: List<String>?,
    ): Passage {
        return testPassages.firstOrNull { passage ->
            passage.book == book &&
                passage.chapter == chapter &&
                passage.translation == translation &&
                passage.id.range == range
        } ?: throw IllegalArgumentException("Test passage not found: $book $chapter $translation")
    }
}
