package dev.mskelton.versly.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.waitUntilAtLeastOneExists
import androidx.navigation3.runtime.rememberNavBackStack
import dev.mskelton.versly.LocalBackStack
import dev.mskelton.versly.LocalReadViewModel
import dev.mskelton.versly.Memory
import dev.mskelton.versly.PickMemoryVerseScreen
import dev.mskelton.versly.persistence.BookMetadata
import dev.mskelton.versly.persistence.LocalAppPreferences
import dev.mskelton.versly.persistence.LocalVerslyDatabase
import dev.mskelton.versly.persistence.MemoryViewModel
import dev.mskelton.versly.testutil.ComposeScreenshotTest
import dev.mskelton.versly.testutil.TestAppPreferences
import dev.mskelton.versly.testutil.TestData
import dev.mskelton.versly.testutil.TestReadViewModel
import dev.mskelton.versly.testutil.TestVerslyDatabase
import dev.mskelton.versly.ui.theme.VerslyTheme
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class PickMemoryVerseScreenTest : ComposeScreenshotTest() {
    private fun setContent(
        books: List<BookMetadata> = listOf(BookMetadata("GEN", "Genesis", "Gen", 50)),
    ) {
        val db = TestVerslyDatabase(emptyList())
        val vm = MemoryViewModel(db = db, navKey = Memory)
        val passage = TestData.fullChapterPassage()
        val readViewModel = TestReadViewModel(listOf(passage), books = books)

        composeTestRule.setContent {
            VerslyTheme {
                val backStack = rememberNavBackStack(Memory)
                CompositionLocalProvider(
                    LocalVerslyDatabase provides db,
                    LocalBackStack provides backStack,
                    LocalAppPreferences provides TestAppPreferences(),
                    LocalReadViewModel provides readViewModel,
                ) {
                    PickMemoryVerseScreen(viewModel = vm)
                }
            }
        }
    }

    @Test
    fun testPickMemoryVerseScreen_showsTitle() {
        setContent()

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Add memory verse").assertIsDisplayed()
    }

    @Test
    fun testPickMemoryVerseScreen_showsTestamentTabs() {
        setContent()

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Old Testament").assertIsDisplayed()
        composeTestRule.onNodeWithText("New Testament").assertIsDisplayed()
    }

    @Test
    fun testPickMemoryVerseScreen_showsBooks() {
        val books = listOf(
            BookMetadata("GEN", "Genesis", "Gen", 50),
            BookMetadata("EXO", "Exodus", "Exo", 40),
        )
        setContent(books = books)

        composeTestRule.waitUntilAtLeastOneExists(hasText("Genesis"), 5000)
        composeTestRule.onNodeWithText("Genesis").assertIsDisplayed()
        composeTestRule.onNodeWithText("Exodus").assertIsDisplayed()
    }

    @Test
    fun testPickMemoryVerseScreen_expandBook_showsChapters() {
        setContent()

        composeTestRule.waitUntilAtLeastOneExists(hasText("Genesis"), 5000)
        composeTestRule.onNodeWithText("Genesis").performClick()

        composeTestRule.waitUntilAtLeastOneExists(hasText("1"), 5000)
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
    }
}
