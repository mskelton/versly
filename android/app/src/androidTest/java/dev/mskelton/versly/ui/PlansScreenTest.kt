package dev.mskelton.versly.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation3.runtime.rememberNavBackStack
import dev.mskelton.versly.LocalBackStack
import dev.mskelton.versly.Plans
import dev.mskelton.versly.PlansScreen
import dev.mskelton.versly.persistence.LocalAppPreferences
import dev.mskelton.versly.testutil.TestAppPreferences
import dev.mskelton.versly.testutil.TestData
import dev.mskelton.versly.testutil.TestPlansViewModel
import dev.mskelton.versly.ui.theme.VerslyTheme
import org.junit.Rule
import org.junit.Test

class PlansScreenTest {
    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun testPlansScreen_rendersMultiplePassages() {
        val passages = TestData.multiplePassages()
        val viewModel = TestPlansViewModel(passages)
        val testAppPreferences = TestAppPreferences()

        composeTestRule.setContent {
            VerslyTheme {
                val backStack = rememberNavBackStack(Plans)
                CompositionLocalProvider(
                    LocalAppPreferences provides testAppPreferences,
                    LocalBackStack provides backStack,
                ) {
                    PlansScreen(viewModel = viewModel)
                }
            }
        }

        composeTestRule.waitForIdle()

        // Verify all passages are displayed in preview
        composeTestRule.onNodeWithText("Genesis", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Matthew", substring = true).assertIsDisplayed()
    }

    @Test
    fun testPlansScreen_rendersPartialRange() {
        // Create a passage with partial range (verses 1-3)
        val passage =
            TestData.partialRangePassage(
                book = "GEN",
                chapter = "1",
                translation = "ESV",
                bookTitle = "Genesis",
                startVerse = 1,
                endVerse = 3,
            )
        val viewModel = TestPlansViewModel(listOf(passage))
        val testAppPreferences = TestAppPreferences()

        composeTestRule.setContent {
            VerslyTheme {
                val backStack = rememberNavBackStack(Plans)
                CompositionLocalProvider(
                    LocalAppPreferences provides testAppPreferences,
                    LocalBackStack provides backStack,
                ) {
                    PlansScreen(viewModel = viewModel)
                }
            }
        }

        composeTestRule.waitForIdle()

        // Verify passage with partial range is displayed
        composeTestRule.onNodeWithText("Genesis", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("1", substring = true).assertIsDisplayed()

        // Verify only verses in range are shown (1-3)
        composeTestRule.onNodeWithText("Text for verse 1", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Text for verse 2", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Text for verse 3", substring = true).assertIsDisplayed()
    }

    @Test
    fun testPlansScreen_rendersFullChapter() {
        val passage = TestData.fullChapterPassage()
        val viewModel = TestPlansViewModel(listOf(passage))
        val testAppPreferences = TestAppPreferences()

        composeTestRule.setContent {
            VerslyTheme {
                val backStack = rememberNavBackStack(Plans)
                CompositionLocalProvider(
                    LocalAppPreferences provides testAppPreferences,
                    LocalBackStack provides backStack,
                ) {
                    PlansScreen(viewModel = viewModel)
                }
            }
        }

        composeTestRule.waitForIdle()

        // Verify full chapter passage is displayed
        composeTestRule.onNodeWithText("Genesis", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("In the beginning", substring = true).assertIsDisplayed()
    }

    @Test
    fun testPlansScreen_mixedRanges() {
        // Mix of full chapters and partial ranges
        val passages =
            listOf(
                TestData.fullChapterPassage("GEN", "1", "ESV", "Genesis"),
                TestData.partialRangePassage("GEN", "2", "ESV", "Genesis", 1, 5),
                TestData.fullChapterPassage("MAT", "5", "ESV", "Matthew"),
            )
        val viewModel = TestPlansViewModel(passages)
        val testAppPreferences = TestAppPreferences()

        composeTestRule.setContent {
            VerslyTheme {
                val backStack = rememberNavBackStack(Plans)
                CompositionLocalProvider(
                    LocalAppPreferences provides testAppPreferences,
                    LocalBackStack provides backStack,
                ) {
                    PlansScreen(viewModel = viewModel)
                }
            }
        }

        composeTestRule.waitForIdle()

        // Verify all passages are displayed
        composeTestRule.onNodeWithText("Genesis", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Matthew", substring = true).assertIsDisplayed()

        // Verify partial range content is shown
        composeTestRule.onNodeWithText("Text for verse 1", substring = true).assertIsDisplayed()
    }

    @Test
    fun testPlansScreen_planPreviewShowsCorrectTitles() {
        val passages =
            listOf(
                TestData.fullChapterPassage("GEN", "1", "ESV", "Genesis"),
                TestData.fullChapterPassage("GEN", "2", "ESV", "Genesis"),
                TestData.fullChapterPassage("MAT", "5", "ESV", "Matthew"),
            )
        val viewModel = TestPlansViewModel(passages)
        val testAppPreferences = TestAppPreferences()

        composeTestRule.setContent {
            VerslyTheme {
                val backStack = rememberNavBackStack(Plans)
                CompositionLocalProvider(
                    LocalAppPreferences provides testAppPreferences,
                    LocalBackStack provides backStack,
                ) {
                    PlansScreen(viewModel = viewModel)
                }
            }
        }

        composeTestRule.waitForIdle()

        // Verify PlanPreview shows correct book titles and chapters
        composeTestRule.onNodeWithText("Genesis 1", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Genesis 2", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Matthew 5", substring = true).assertIsDisplayed()
    }
}
