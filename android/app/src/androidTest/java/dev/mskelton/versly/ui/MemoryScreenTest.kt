package dev.mskelton.versly.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.waitUntilAtLeastOneExists
import androidx.navigation3.runtime.rememberNavBackStack
import dev.mskelton.versly.LocalBackStack
import dev.mskelton.versly.Memory
import dev.mskelton.versly.MemoryScreen
import dev.mskelton.versly.persistence.LocalVerslyDatabase
import dev.mskelton.versly.persistence.MemoryViewModel
import dev.mskelton.versly.testutil.ComposeScreenshotTest
import dev.mskelton.versly.testutil.TestVerslyDatabase
import dev.mskelton.versly.ui.theme.VerslyTheme
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class MemoryScreenTest : ComposeScreenshotTest() {
    private fun createDbAndViewModel(): Pair<TestVerslyDatabase, MemoryViewModel> {
        val db = TestVerslyDatabase(emptyList())
        val vm = MemoryViewModel(db = db, navKey = Memory)
        return db to vm
    }

    private fun setContent(
        db: TestVerslyDatabase,
        vm: MemoryViewModel,
    ) {
        composeTestRule.setContent {
            VerslyTheme {
                val backStack = rememberNavBackStack(Memory)
                CompositionLocalProvider(
                    LocalVerslyDatabase provides db,
                    LocalBackStack provides backStack,
                ) {
                    MemoryScreen(viewModel = vm)
                }
            }
        }
    }

    @Test
    fun testMemoryScreen_emptyState_showsMessage() {
        val (db, vm) = createDbAndViewModel()

        setContent(db, vm)

        composeTestRule.waitUntilAtLeastOneExists(hasText("No saved verses yet", substring = true), 5000)
        composeTestRule.onNodeWithText("No saved verses yet", substring = true).assertIsDisplayed()
    }

    @Test
    fun testMemoryScreen_showsTitle() {
        val (db, vm) = createDbAndViewModel()

        setContent(db, vm)

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Memory").assertIsDisplayed()
    }

    @Test
    fun testMemoryScreen_showsAddButton() {
        val (db, vm) = createDbAndViewModel()

        setContent(db, vm)

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Add verse").assertIsDisplayed()
    }

    @Test
    fun testMemoryScreen_withVerses_displaysReference() {
        val db = TestVerslyDatabase(emptyList())
        db.saveMemoryVerse(
            book = "GEN",
            chapter = "1",
            verseStart = "1",
            verseEnd = null,
            translation = "ESV",
            text = "In the beginning, God created the heavens and the earth.",
            reference = "Genesis 1:1",
        )
        val vm = MemoryViewModel(db = db, navKey = Memory)

        setContent(db, vm)

        composeTestRule.waitUntilAtLeastOneExists(hasText("Genesis 1:1"), 5000)
        composeTestRule.onAllNodesWithText("Genesis 1:1").assertAny(hasText("Genesis 1:1"))
    }

    @Test
    fun testMemoryScreen_withVerses_displaysVerseText() {
        val db = TestVerslyDatabase(emptyList())
        db.saveMemoryVerse(
            book = "GEN",
            chapter = "1",
            verseStart = "1",
            verseEnd = null,
            translation = "ESV",
            text = "In the beginning, God created the heavens and the earth.",
            reference = "Genesis 1:1",
        )
        val vm = MemoryViewModel(db = db, navKey = Memory)

        setContent(db, vm)

        composeTestRule.waitUntilAtLeastOneExists(hasText("In the beginning", substring = true), 5000)
        composeTestRule.onAllNodesWithText("In the beginning", substring = true).assertAny(hasText("In the beginning", substring = true))
    }

    @Test
    fun testMemoryScreen_withMultipleVerses_displaysAll() {
        val db = TestVerslyDatabase(emptyList())
        db.saveMemoryVerse(
            book = "GEN",
            chapter = "1",
            verseStart = "1",
            verseEnd = null,
            translation = "ESV",
            text = "In the beginning, God created the heavens and the earth.",
            reference = "Genesis 1:1",
        )
        db.saveMemoryVerse(
            book = "JHN",
            chapter = "3",
            verseStart = "16",
            verseEnd = null,
            translation = "ESV",
            text = "For God so loved the world.",
            reference = "John 3:16",
        )
        val vm = MemoryViewModel(db = db, navKey = Memory)

        setContent(db, vm)

        composeTestRule.waitUntilAtLeastOneExists(hasText("Genesis 1:1"), 5000)
        composeTestRule.onAllNodesWithText("Genesis 1:1").assertAny(hasText("Genesis 1:1"))
        composeTestRule.onAllNodesWithText("John 3:16").assertAny(hasText("John 3:16"))
    }

    @Test
    fun testMemoryScreen_withVerseRange_displaysReference() {
        val db = TestVerslyDatabase(emptyList())
        db.saveMemoryVerse(
            book = "ROM",
            chapter = "8",
            verseStart = "28",
            verseEnd = "30",
            translation = "ESV",
            text = "And we know that for those who love God all things work together for good.",
            reference = "Romans 8:28–30",
        )
        val vm = MemoryViewModel(db = db, navKey = Memory)

        setContent(db, vm)

        composeTestRule.waitUntilAtLeastOneExists(hasText("Romans 8:28–30"), 5000)
        composeTestRule.onAllNodesWithText("Romans 8:28–30").assertAny(hasText("Romans 8:28–30"))
    }
}
