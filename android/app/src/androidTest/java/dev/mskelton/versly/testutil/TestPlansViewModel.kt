package dev.mskelton.versly.testutil

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import dev.mskelton.versly.persistence.Passage
import dev.mskelton.versly.persistence.PlansViewModel
import dev.mskelton.versly.persistence.Reading

/** Test PlansViewModel that uses a fake VerslyDatabase with test data */
class TestPlansViewModel(
    testPassages: List<Passage>,
    context: Context =
        androidx.test.platform.app.InstrumentationRegistry
            .getInstrumentation()
            .targetContext,
) : PlansViewModel(
        db = TestVerslyDatabase(testPassages, context),
        savedStateHandle = SavedStateHandle(),
        appPreferences = TestAppPreferences(context),
        planProvider = TestPlanProvider(testPassages.map { Reading(it.book, it.chapter, it.id.range) }),
    )
