package dev.mskelton.versly.testutil

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import dev.mskelton.versly.Plans
import dev.mskelton.versly.persistence.Passage
import dev.mskelton.versly.persistence.PlansViewModel

/** Test PlansViewModel that uses a fake BibleDatabase with test data */
class TestPlansViewModel(
    testPassages: List<Passage>,
    context: Context =
        androidx.test.platform.app.InstrumentationRegistry
            .getInstrumentation()
            .targetContext,
) : PlansViewModel(
        bibleDatabase = TestBibleDatabase(testPassages, context),
        savedStateHandle = SavedStateHandle(),
        navKey = Plans,
    ) {
    // isLoading is already available from PlansViewModel, just initialize it
    init {
        setLoading(false)
    }
}
