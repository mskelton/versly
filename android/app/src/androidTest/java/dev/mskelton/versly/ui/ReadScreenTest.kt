package dev.mskelton.versly.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.waitUntilAtLeastOneExists
import androidx.lifecycle.SavedStateHandle
import androidx.navigation3.runtime.rememberNavBackStack
import dev.mskelton.versly.LocalBackStack
import dev.mskelton.versly.Read
import dev.mskelton.versly.ReadScreen
import dev.mskelton.versly.persistence.BookMetadata
import dev.mskelton.versly.persistence.LocalAppPreferences
import dev.mskelton.versly.persistence.LocalBibleDatabase
import dev.mskelton.versly.persistence.Passage
import dev.mskelton.versly.persistence.PassageId
import dev.mskelton.versly.persistence.ReadViewModel
import dev.mskelton.versly.persistence.encodePassageId
import dev.mskelton.versly.testutil.ComposeScreenshotTest
import dev.mskelton.versly.testutil.TestAppPreferences
import dev.mskelton.versly.testutil.TestBibleDatabase
import dev.mskelton.versly.testutil.TestData
import dev.mskelton.versly.ui.theme.VerslyTheme
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class ReadScreenTest : ComposeScreenshotTest() {
    private val translation = "ESV"

    private fun setup(
        passages: List<Passage>,
        books: List<BookMetadata>,
        initialPassage: PassageId,
    ): Pair<TestBibleDatabase, ReadViewModel> {
        val db = TestBibleDatabase(passages).apply { books.forEach { addBook(it) } }
        val vm =
            ReadViewModel(
                bibleDatabase = db,
                savedStateHandle = SavedStateHandle(mapOf("current_passage_id" to encodePassageId(initialPassage))),
                appPreferences = TestAppPreferences(),
                navKey = Read,
            )
        return db to vm
    }

    private fun setContent(
        db: TestBibleDatabase,
        vm: ReadViewModel,
    ) {
        composeTestRule.setContent {
            VerslyTheme {
                val backStack = rememberNavBackStack(Read)
                CompositionLocalProvider(
                    LocalBibleDatabase provides db,
                    LocalAppPreferences provides TestAppPreferences(),
                    LocalBackStack provides backStack,
                ) {
                    ReadScreen(viewModel = vm)
                }
            }
        }
    }

    // region content

    @Test
    fun testReadScreen_displaysVerseText() {
        val passage = TestData.fullChapterPassage("GEN", "1", translation, "Genesis")
        val (db, vm) = setup(listOf(passage), listOf(BookMetadata("GEN", "Genesis", "Gen", 50)), passage.id)

        setContent(db, vm)

        composeTestRule.waitUntilAtLeastOneExists(hasText("In the beginning", substring = true), 5000)
        composeTestRule.onNodeWithText("In the beginning", substring = true).assertIsDisplayed()
    }

    // endregion

    // region toolbar

    @Test
    fun testReadScreen_toolbarShowsBookTitleAndChapter() {
        val passage = TestData.fullChapterPassage("MAT", "5", translation, "Matthew")
        val (db, vm) = setup(listOf(passage), listOf(BookMetadata("MAT", "Matthew", "Mat", 28)), passage.id)

        setContent(db, vm)

        composeTestRule.waitUntilAtLeastOneExists(hasText("Matthew 5"), 5000)
        composeTestRule.onNodeWithText("Matthew 5").assertIsDisplayed()
    }

    @Test
    fun testReadScreen_toolbarShowsTranslation() {
        val passage = TestData.fullChapterPassage("GEN", "1", translation, "Genesis")
        val (db, vm) = setup(listOf(passage), listOf(BookMetadata("GEN", "Genesis", "Gen", 50)), passage.id)

        setContent(db, vm)

        composeTestRule.waitUntilAtLeastOneExists(hasText(translation), 5000)
        composeTestRule.onNodeWithText(translation).assertIsDisplayed()
    }

    // endregion

    // region navigation - within book

    @Test
    fun testReadScreen_nextChapter_updatesToolbar() {
        val chapter1 = TestData.fullChapterPassage("GEN", "1", translation, "Genesis")
        val chapter2 = TestData.fullChapterPassage("GEN", "2", translation, "Genesis")
        val (db, vm) =
            setup(listOf(chapter1, chapter2), listOf(BookMetadata("GEN", "Genesis", "Gen", 50)), chapter1.id)

        setContent(db, vm)

        composeTestRule.waitUntilAtLeastOneExists(hasText("Genesis 1"), 5000)
        composeTestRule.onNodeWithContentDescription("Next chapter").performClick()

        composeTestRule.waitUntilAtLeastOneExists(hasText("Genesis 2"), 5000)
        composeTestRule.onNodeWithText("Genesis 2").assertIsDisplayed()
    }

    @Test
    fun testReadScreen_previousChapter_updatesToolbar() {
        val chapter1 = TestData.fullChapterPassage("GEN", "1", translation, "Genesis")
        val chapter2 = TestData.fullChapterPassage("GEN", "2", translation, "Genesis")
        val (db, vm) =
            setup(listOf(chapter1, chapter2), listOf(BookMetadata("GEN", "Genesis", "Gen", 50)), chapter2.id)

        setContent(db, vm)

        composeTestRule.waitUntilAtLeastOneExists(hasText("Genesis 2"), 5000)
        composeTestRule.onNodeWithContentDescription("Previous chapter").performClick()

        composeTestRule.waitUntilAtLeastOneExists(hasText("Genesis 1"), 5000)
        composeTestRule.onNodeWithText("Genesis 1").assertIsDisplayed()
    }

    // endregion

    // region navigation - cross book

    @Test
    fun testReadScreen_nextAtLastChapter_goesToFirstChapterOfNextBook() {
        val genesis50 = TestData.fullChapterPassage("GEN", "50", translation, "Genesis")
        val exodus1 = TestData.fullChapterPassage("EXO", "1", translation, "Exodus")
        val books = listOf(BookMetadata("GEN", "Genesis", "Gen", 50), BookMetadata("EXO", "Exodus", "Exo", 40))
        val (db, vm) = setup(listOf(genesis50, exodus1), books, genesis50.id)

        setContent(db, vm)

        composeTestRule.waitUntilAtLeastOneExists(hasText("Genesis 50"), 5000)
        composeTestRule.onNodeWithContentDescription("Next chapter").performClick()

        composeTestRule.waitUntilAtLeastOneExists(hasText("Exodus 1"), 5000)
        composeTestRule.onNodeWithText("Exodus 1").assertIsDisplayed()
    }

    @Test
    fun testReadScreen_previousAtFirstChapter_goesToLastChapterOfPreviousBook() {
        val genesis50 = TestData.fullChapterPassage("GEN", "50", translation, "Genesis")
        val exodus1 = TestData.fullChapterPassage("EXO", "1", translation, "Exodus")
        val books = listOf(BookMetadata("GEN", "Genesis", "Gen", 50), BookMetadata("EXO", "Exodus", "Exo", 40))
        val (db, vm) = setup(listOf(genesis50, exodus1), books, exodus1.id)

        setContent(db, vm)

        composeTestRule.waitUntilAtLeastOneExists(hasText("Exodus 1"), 5000)
        composeTestRule.onNodeWithContentDescription("Previous chapter").performClick()

        composeTestRule.waitUntilAtLeastOneExists(hasText("Genesis 50"), 5000)
        composeTestRule.onNodeWithText("Genesis 50").assertIsDisplayed()
    }

    // endregion

    // region navigation - boundaries

    @Test
    fun testReadScreen_previousAtFirstChapterOfFirstBook_doesNotNavigate() {
        val chapter1 = TestData.fullChapterPassage("GEN", "1", translation, "Genesis")
        val (db, vm) = setup(listOf(chapter1), listOf(BookMetadata("GEN", "Genesis", "Gen", 50)), chapter1.id)

        setContent(db, vm)

        composeTestRule.waitUntilAtLeastOneExists(hasText("Genesis 1"), 5000)
        composeTestRule.onNodeWithContentDescription("Previous chapter").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Genesis 1").assertIsDisplayed()
    }

    @Test
    fun testReadScreen_nextAtLastChapterOfLastBook_doesNotNavigate() {
        val lastChapter = TestData.fullChapterPassage("REV", "22", translation, "Revelation")
        val (db, vm) =
            setup(listOf(lastChapter), listOf(BookMetadata("REV", "Revelation", "Rev", 22)), lastChapter.id)

        setContent(db, vm)

        composeTestRule.waitUntilAtLeastOneExists(hasText("Revelation 22"), 5000)
        composeTestRule.onNodeWithContentDescription("Next chapter").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Revelation 22").assertIsDisplayed()
    }

    // endregion
}
