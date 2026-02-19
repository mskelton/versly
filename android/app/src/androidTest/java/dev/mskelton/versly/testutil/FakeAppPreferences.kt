package dev.mskelton.versly.testutil

import android.content.Context
import dev.mskelton.versly.persistence.AppPreferences
import dev.mskelton.versly.persistence.PassageId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Fake AppPreferences for testing Uses a real Context but provides controllable flows */
class FakeAppPreferences(
    context: Context =
        androidx.test.platform.app.InstrumentationRegistry
            .getInstrumentation()
            .targetContext,
) : AppPreferences(context) {
    private val _translation = MutableStateFlow("ESV")
    private val _passage = MutableStateFlow<PassageId?>(null)

    override val translation: Flow<String> = _translation
    override val passage: Flow<PassageId> = _passage.filterNotNull()

    override suspend fun setTranslation(translation: String) {
        _translation.value = translation
    }

    override suspend fun setPassage(passageId: PassageId) {
        _passage.value = passageId
    }
}
