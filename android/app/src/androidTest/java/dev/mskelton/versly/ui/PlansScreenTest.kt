package dev.mskelton.versly.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.waitUntilAtLeastOneExists
import androidx.navigation3.runtime.rememberNavBackStack
import dev.mskelton.versly.LocalBackStack
import dev.mskelton.versly.Plans
import dev.mskelton.versly.PlansScreen
import dev.mskelton.versly.testutil.ComposeScreenshotTest
import dev.mskelton.versly.testutil.TestData
import dev.mskelton.versly.testutil.TestPlansViewModel
import dev.mskelton.versly.ui.theme.VerslyTheme
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class PlansScreenTest : ComposeScreenshotTest() {
    @Test
    fun testPlansScreen_rendersMultiplePassages() {
        val passages = TestData.multiplePassages()
        val viewModel = TestPlansViewModel(passages)

        composeTestRule.setContent {
            VerslyTheme {
                val backStack = rememberNavBackStack(Plans)
                CompositionLocalProvider(LocalBackStack provides backStack) {
                    PlansScreen(viewModel = viewModel)
                }
            }
        }

        composeTestRule.waitUntilAtLeastOneExists(hasText("Genesis", substring = true), 5000)

        composeTestRule.onAllNodesWithText("Genesis", substring = true).assertAny(hasText("Genesis", substring = true))
        composeTestRule.onAllNodesWithText("Matthew", substring = true).assertAny(hasText("Matthew", substring = true))
    }

    @Test
    fun testPlansScreen_rendersPartialRange() {
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

        composeTestRule.setContent {
            VerslyTheme {
                val backStack = rememberNavBackStack(Plans)
                CompositionLocalProvider(LocalBackStack provides backStack) {
                    PlansScreen(viewModel = viewModel)
                }
            }
        }

        composeTestRule.waitUntilAtLeastOneExists(hasText("Genesis", substring = true), 5000)

        composeTestRule.onAllNodesWithText("Genesis", substring = true).assertAny(hasText("Genesis", substring = true))
        composeTestRule.onNodeWithTag("plansList").performScrollToNode(hasText("Text for verse 1", substring = true))
        composeTestRule.onNodeWithText("Text for verse 1", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Text for verse 2", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Text for verse 3", substring = true).assertIsDisplayed()
    }

    @Test
    fun testPlansScreen_rendersFullChapter() {
        val passage = TestData.fullChapterPassage()
        val viewModel = TestPlansViewModel(listOf(passage))

        composeTestRule.setContent {
            VerslyTheme {
                val backStack = rememberNavBackStack(Plans)
                CompositionLocalProvider(LocalBackStack provides backStack) {
                    PlansScreen(viewModel = viewModel)
                }
            }
        }

        composeTestRule.waitUntilAtLeastOneExists(hasText("Genesis", substring = true), 5000)

        composeTestRule.onAllNodesWithText("Genesis", substring = true).assertAny(hasText("Genesis", substring = true))
        composeTestRule.onNodeWithTag("plansList").performScrollToNode(hasText("In the beginning", substring = true))
        composeTestRule.onNodeWithText("In the beginning", substring = true).assertIsDisplayed()
    }

    @Test
    fun testPlansScreen_mixedRanges() {
        val passages =
            listOf(
                TestData.fullChapterPassage("GEN", "1", "ESV", "Genesis"),
                TestData.partialRangePassage("GEN", "2", "ESV", "Genesis", 1, 5),
                TestData.fullChapterPassage("MAT", "5", "ESV", "Matthew"),
            )
        val viewModel = TestPlansViewModel(passages)

        composeTestRule.setContent {
            VerslyTheme {
                val backStack = rememberNavBackStack(Plans)
                CompositionLocalProvider(LocalBackStack provides backStack) {
                    PlansScreen(viewModel = viewModel)
                }
            }
        }

        composeTestRule.waitUntilAtLeastOneExists(hasText("Genesis", substring = true), 5000)

        composeTestRule.onAllNodesWithText("Genesis", substring = true).assertAny(hasText("Genesis", substring = true))
        composeTestRule.onAllNodesWithText("Matthew", substring = true).assertAny(hasText("Matthew", substring = true))
        composeTestRule.onNodeWithTag("plansList").performScrollToNode(hasText("Text for verse 1", substring = true))
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

        composeTestRule.setContent {
            VerslyTheme {
                val backStack = rememberNavBackStack(Plans)
                CompositionLocalProvider(LocalBackStack provides backStack) {
                    PlansScreen(viewModel = viewModel)
                }
            }
        }

        composeTestRule.waitUntilAtLeastOneExists(hasText("Genesis 1", substring = true), 5000)

        composeTestRule.onNodeWithText("Genesis 1", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Genesis 2", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Matthew 5", substring = true).assertIsDisplayed()
    }
}
