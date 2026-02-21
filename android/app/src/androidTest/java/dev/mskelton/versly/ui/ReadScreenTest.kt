package dev.mskelton.versly.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation3.runtime.rememberNavBackStack
import dev.mskelton.versly.LocalBackStack
import dev.mskelton.versly.Read
import dev.mskelton.versly.ReadScreenContent
import dev.mskelton.versly.persistence.BookMetadata
import dev.mskelton.versly.persistence.LocalAppPreferences
import dev.mskelton.versly.persistence.LocalBibleDatabase
import dev.mskelton.versly.testutil.TestAppPreferences
import dev.mskelton.versly.testutil.TestBibleDatabase
import dev.mskelton.versly.testutil.TestData
import dev.mskelton.versly.testutil.TestReadViewModel
import dev.mskelton.versly.ui.theme.VerslyTheme
import org.junit.Rule
import org.junit.Test

class ReadScreenTest {
    @get:Rule val composeTestRule = createComposeRule()

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
                        onPassageIdChange = {},
                        viewModel = viewModel,
                    )
                }
            }
        }

        // Wait for content to load
        composeTestRule.waitForIdle()

        // Verify chapter header is displayed
        composeTestRule.onNodeWithText("Genesis", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("1", substring = true).assertIsDisplayed()

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
                        onPassageIdChange = {},
                        viewModel = viewModel,
                    )
                }
            }
        }

        composeTestRule.waitForIdle()

        // Verify chapter header shows book title and chapter
        composeTestRule.onNodeWithText("Matthew", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("5", substring = true).assertIsDisplayed()
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
                        onPassageIdChange = {},
                        viewModel = viewModel,
                    )
                }
            }
        }

        composeTestRule.waitForIdle()

        // Verify various node types are displayed
        // Chapter header
        composeTestRule.onNodeWithText("Genesis", substring = true).assertIsDisplayed()
        // Verse markers and text
        composeTestRule.onNodeWithText("1", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("In the beginning", substring = true).assertIsDisplayed()
    }
}
