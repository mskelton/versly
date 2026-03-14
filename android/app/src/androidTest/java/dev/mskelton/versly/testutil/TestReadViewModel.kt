package dev.mskelton.versly.testutil

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import dev.mskelton.versly.persistence.BookMetadata
import dev.mskelton.versly.persistence.Passage
import dev.mskelton.versly.persistence.ReadViewModel
import dev.mskelton.versly.persistence.encodePassageId

/** Test ReadViewModel that uses a fake VerslyDatabase with test data */
class TestReadViewModel(
    testPassages: List<Passage>,
    books: List<BookMetadata> = emptyList(),
    context: Context =
        androidx.test.platform.app.InstrumentationRegistry
            .getInstrumentation()
            .targetContext,
) : ReadViewModel(
        db =
            TestVerslyDatabase(testPassages, context).apply {
                books.forEach { addBook(it) }
            },
        savedStateHandle =
            if (testPassages.isNotEmpty()) {
                SavedStateHandle(mapOf("current_passage_id" to encodePassageId(testPassages.first().id)))
            } else {
                SavedStateHandle()
            },
        appPreferences = TestAppPreferences(context),
    )
