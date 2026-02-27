package dev.mskelton.versly.testutil

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import dev.mskelton.versly.Read
import dev.mskelton.versly.persistence.Passage
import dev.mskelton.versly.persistence.ReadViewModel
import dev.mskelton.versly.testutil.TestBibleDatabase

/** Test ReadViewModel that uses a fake BibleDatabase with test data */
class TestReadViewModel(
    testPassages: List<Passage>,
    context: Context =
        androidx.test.platform.app.InstrumentationRegistry
            .getInstrumentation()
            .targetContext,
) : ReadViewModel(
        bibleDatabase = TestBibleDatabase(testPassages, context),
        savedStateHandle = SavedStateHandle(),
        appPreferences = TestAppPreferences(context),
        navKey = Read,
    )
