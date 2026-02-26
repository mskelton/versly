package dev.mskelton.versly.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation3.runtime.rememberNavBackStack
import dev.mskelton.versly.LocalBackStack
import dev.mskelton.versly.Read
import dev.mskelton.versly.ReadScreenContent
import dev.mskelton.versly.persistence.BookMetadata
import dev.mskelton.versly.persistence.LocalAppPreferences
import dev.mskelton.versly.persistence.LocalBibleDatabase
import dev.mskelton.versly.testutil.ComposeScreenshotTest
import dev.mskelton.versly.testutil.TestAppPreferences
import dev.mskelton.versly.testutil.TestBibleDatabase
import dev.mskelton.versly.testutil.TestData
import dev.mskelton.versly.testutil.TestReadViewModel
import dev.mskelton.versly.ui.theme.VerslyTheme
import org.junit.Test

class ReadScreenTest : ComposeScreenshotTest() {
    @Test
    fun testReadScreen_rendersFullChapter() {
        val passage = TestData.fullChapterPassage()
        val viewModel = TestReadViewModel(listOf(passage))
        val testBibleDatabase =
            TestBibleDatabase(testPassages = listOf(passage)).apply { addBook(BookMetadata("GEN", "Genesis", "Gen", 50)) }
        val testAppPreferences = TestAppPreferences()

        composeTestRule.setContent {
            VerslyTheme {
                val backStack = rememberNavBackStack(Read())
                CompositionLocalProvider(
                    LocalBibleDatabase provides testBibleDatabase,
                    LocalAppPreferences provides testAppPreferences,
                    LocalBackStack provides backStack,
                ) {
                    ReadScreenContent(
                        passageId = passage.id,
                        viewModel = viewModel,
                    )
                }
            }
        }

        // Wait for content to load
        composeTestRule.waitForIdle()

        // Verify chapter header is displayed
        composeTestRule.onAllNodesWithText("Genesis", substring = true).assertAny(hasText("Genesis", substring = true))

        // Verify verse content is displayed
        composeTestRule.onNodeWithText("In the beginning", substring = true).assertIsDisplayed()
    }

    @Test
    fun testReadScreen_displaysChapterHeader() {
        val passage = TestData.fullChapterPassage("MAT", "5", "ESV", "Matthew")
        val viewModel = TestReadViewModel(listOf(passage))
        val testBibleDatabase =
            TestBibleDatabase(testPassages = listOf(passage)).apply { addBook(BookMetadata("MAT", "Matthew", "Mat", 28)) }
        val testAppPreferences = TestAppPreferences()

        composeTestRule.setContent {
            VerslyTheme {
                val backStack = rememberNavBackStack(Read())
                CompositionLocalProvider(
                    LocalBibleDatabase provides testBibleDatabase,
                    LocalAppPreferences provides testAppPreferences,
                    LocalBackStack provides backStack,
                ) {
                    ReadScreenContent(
                        passageId = passage.id,
                        viewModel = viewModel,
                    )
                }
            }
        }

        composeTestRule.waitForIdle()

        // Verify chapter header shows book title and chapter
        composeTestRule.onAllNodesWithText("Matthew", substring = true).assertAny(hasText("Matthew", substring = true))
    }

    @Test
    fun testReadScreen_displaysAllNodes() {
        val passage = TestData.fullChapterPassage()
        val viewModel = TestReadViewModel(listOf(passage))
        val testBibleDatabase =
            TestBibleDatabase(testPassages = listOf(passage)).apply { addBook(BookMetadata("GEN", "Genesis", "Gen", 50)) }
        val testAppPreferences = TestAppPreferences()

        composeTestRule.setContent {
            VerslyTheme {
                val backStack = rememberNavBackStack(Read())
                CompositionLocalProvider(
                    LocalBibleDatabase provides testBibleDatabase,
                    LocalAppPreferences provides testAppPreferences,
                    LocalBackStack provides backStack,
                ) {
                    ReadScreenContent(
                        passageId = passage.id,
                        viewModel = viewModel,
                    )
                }
            }
        }

        composeTestRule.waitForIdle()

        // Verify various node types are displayed
        // Chapter header
        composeTestRule.onAllNodesWithText("Genesis", substring = true).assertAny(hasText("Genesis", substring = true))
        // Verse text
        composeTestRule.onNodeWithText("In the beginning", substring = true).assertIsDisplayed()
    }
}
