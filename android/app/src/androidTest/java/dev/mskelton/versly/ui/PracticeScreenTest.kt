package dev.mskelton.versly.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation3.runtime.rememberNavBackStack
import dev.mskelton.versly.LocalBackStack
import dev.mskelton.versly.PracticeScreen
import dev.mskelton.versly.Read
import dev.mskelton.versly.persistence.LocalVerslyDatabase
import dev.mskelton.versly.persistence.Passage
import dev.mskelton.versly.testutil.ComposeScreenshotTest
import dev.mskelton.versly.testutil.TestData
import dev.mskelton.versly.testutil.TestVerslyDatabase
import dev.mskelton.versly.ui.theme.VerslyTheme
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class PracticeScreenTest : ComposeScreenshotTest() {
    private fun setContent(
        passages: List<Passage>,
        verse: dev.mskelton.versly.persistence.MemoryVerse = TestData.memoryVerse(),
        onDelete: () -> Unit = {},
    ) {
        val db = TestVerslyDatabase(passages)

        composeTestRule.setContent {
            VerslyTheme {
                val backStack = rememberNavBackStack(Read)
                CompositionLocalProvider(
                    LocalVerslyDatabase provides db,
                    LocalBackStack provides backStack,
                ) {
                    PracticeScreen(verse = verse, onDelete = onDelete)
                }
            }
        }
    }

    private fun singleVersePassage() =
        TestData.passage(
            book = "GEN",
            chapter = "1",
            bookTitle = "Genesis",
            range = listOf("1"),
            nodes =
                listOf(
                    TestData.createChapterNode("GEN.1.0", "Genesis", "1"),
                    TestData.createParagraphNode(
                        "GEN.1.1",
                        listOf(TestData.verseMarker(1), "In the beginning, God created the heavens and the earth."),
                    ),
                ),
        )

    @Test
    fun testPracticeScreen_displaysVerseReference() {
        val verse = TestData.memoryVerse(reference = "Genesis 1:1")
        setContent(listOf(singleVersePassage()), verse)

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Genesis 1:1").assertIsDisplayed()
    }

    @Test
    fun testPracticeScreen_displaysVerseText() {
        val verse = TestData.memoryVerse(verseStart = "1", verseEnd = "2")
        val passage =
            TestData.partialRangePassage(
                book = "GEN",
                chapter = "1",
                startVerse = 1,
                endVerse = 2,
            )

        setContent(listOf(passage), verse)

        composeTestRule.waitUntilAtLeastOneExists(hasText("Text for verse 1", substring = true), 5000)
        composeTestRule.onNodeWithText("Text for verse 1", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Text for verse 2", substring = true).assertIsDisplayed()
    }

    @Test
    fun testPracticeScreen_showsMoreOptionsButton() {
        val verse = TestData.memoryVerse()
        setContent(listOf(singleVersePassage()), verse)

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("More options").assertIsDisplayed()
    }

    @Test
    fun testPracticeScreen_moreMenu_showsDeleteOption() {
        val verse = TestData.memoryVerse()
        setContent(listOf(singleVersePassage()), verse)

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("More options").performClick()
        composeTestRule.onNodeWithText("Delete verse").assertIsDisplayed()
    }

    @Test
    fun testPracticeScreen_deleteInvokesCallback() {
        var deleted = false
        val verse = TestData.memoryVerse()
        setContent(listOf(singleVersePassage()), verse, onDelete = { deleted = true })

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("More options").performClick()
        composeTestRule.onNodeWithText("Delete verse").performClick()

        assertTrue(deleted)
    }

    @Test
    fun testPracticeScreen_noChapterHeaderShown() {
        val verse = TestData.memoryVerse()
        setContent(listOf(singleVersePassage()), verse)

        composeTestRule.waitUntilAtLeastOneExists(hasText("In the beginning", substring = true), 5000)
        composeTestRule.onNodeWithText("In the beginning", substring = true).assertIsDisplayed()
    }

    @Test
    fun testPracticeScreen_verseRange_displaysCorrectReference() {
        val verse =
            TestData.memoryVerse(
                verseStart = "3",
                verseEnd = "5",
                reference = "Genesis 1:3–5",
            )
        val passage =
            TestData.partialRangePassage(
                book = "GEN",
                chapter = "1",
                startVerse = 3,
                endVerse = 5,
            )

        setContent(listOf(passage), verse)

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Genesis 1:3–5").assertIsDisplayed()
    }
}
