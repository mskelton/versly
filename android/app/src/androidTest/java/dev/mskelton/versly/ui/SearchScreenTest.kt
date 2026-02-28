package dev.mskelton.versly.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation3.runtime.rememberNavBackStack
import dev.mskelton.versly.LocalBackStack
import dev.mskelton.versly.LocalReadViewModel
import dev.mskelton.versly.Read
import dev.mskelton.versly.SearchResultItem
import dev.mskelton.versly.SearchScreenContent
import dev.mskelton.versly.persistence.HydratedSearchResult
import dev.mskelton.versly.persistence.PassageId
import dev.mskelton.versly.testutil.ComposeScreenshotTest
import dev.mskelton.versly.testutil.TestReadViewModel
import dev.mskelton.versly.ui.theme.VerslyTheme
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchScreenTest : ComposeScreenshotTest() {
    private fun makeResult(
        bookTitle: String,
        chapter: String,
        text: String = "Sample verse text.",
        range: List<String>? = null,
    ) = HydratedSearchResult(
        passageId = PassageId(book = "GEN", chapter = chapter, translation = "ESV", range = range),
        bookTitle = bookTitle,
        text = text,
    )

    // region SearchResultItem

    @Test
    fun testSearchResultItem_displaysPassageAndText() {
        val result = makeResult("Genesis", "1", "In the beginning God created the heavens and the earth.")

        composeTestRule.setContent {
            VerslyTheme {
                SearchResultItem(result = result, onClick = {})
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Genesis 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("In the beginning", substring = true).assertIsDisplayed()
    }

    @Test
    fun testSearchResultItem_withVerseRange_displaysFormattedPassage() {
        val result =
            makeResult(
                bookTitle = "Romans",
                chapter = "5",
                text = "Therefore, since we have been justified by faith.",
                range = listOf("1", "11"),
            )

        composeTestRule.setContent {
            VerslyTheme {
                SearchResultItem(result = result, onClick = {})
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Romans 5:1-11").assertIsDisplayed()
    }

    @Test
    fun testSearchResultItem_withSingleVerse_displaysFormattedPassage() {
        val result =
            makeResult(
                bookTitle = "John",
                chapter = "3",
                text = "For God so loved the world.",
                range = listOf("16", "16"),
            )

        composeTestRule.setContent {
            VerslyTheme {
                SearchResultItem(result = result, onClick = {})
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("John 3:16").assertIsDisplayed()
    }

    @Test
    fun testSearchResultItem_onClick_invokesCallback() {
        var clicked = false
        val result = makeResult("Genesis", "1", "In the beginning God created the heavens.")

        composeTestRule.setContent {
            VerslyTheme {
                SearchResultItem(result = result, onClick = { clicked = true })
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Genesis 1").performClick()
        assertTrue(clicked)
    }

    // endregion

    // region SearchScreenContent

    @Test
    fun testSearchScreen_showsSearchBarPlaceholder() {
        val viewModel = TestReadViewModel(emptyList())

        composeTestRule.setContent {
            VerslyTheme {
                val backStack = rememberNavBackStack(Read)
                CompositionLocalProvider(
                    LocalBackStack provides backStack,
                    LocalReadViewModel provides viewModel,
                ) {
                    SearchScreenContent(
                        searchQuery = "",
                        onSearchQueryChange = {},
                        searchResults = emptyList(),
                        isLoading = false,
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Search the Bible", substring = true).assertIsDisplayed()
    }

    // endregion
}
