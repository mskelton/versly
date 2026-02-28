package dev.mskelton.versly.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.mskelton.versly.TranslationManagementRow
import dev.mskelton.versly.persistence.Translation
import dev.mskelton.versly.testutil.ComposeScreenshotTest
import dev.mskelton.versly.ui.theme.VerslyTheme
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsScreenTest : ComposeScreenshotTest() {
    private fun translation(
        id: String = "ESV",
        title: String = "English Standard Version",
        isDownloaded: Boolean = false,
    ) = Translation(
        id = id,
        title = title,
        lastUpdated = "2025-01-01T00:00:00.000Z",
        isDownloaded = isDownloaded,
    )

    // region display

    @Test
    fun testTranslationRow_displaysIdAndTitle() {
        composeTestRule.setContent {
            VerslyTheme {
                TranslationManagementRow(
                    translation = translation("NIV", "New International Version", isDownloaded = false),
                    isLoading = false,
                    isCurrentTranslation = false,
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("NIV").assertIsDisplayed()
        composeTestRule.onNodeWithText("New International Version").assertIsDisplayed()
    }

    @Test
    fun testTranslationRow_notDownloaded_showsDownloadButton() {
        composeTestRule.setContent {
            VerslyTheme {
                TranslationManagementRow(
                    translation = translation(isDownloaded = false),
                    isLoading = false,
                    isCurrentTranslation = false,
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Download translation").assertIsDisplayed()
    }

    @Test
    fun testTranslationRow_downloaded_notCurrent_showsDeleteButton() {
        composeTestRule.setContent {
            VerslyTheme {
                TranslationManagementRow(
                    translation = translation(isDownloaded = true),
                    isLoading = false,
                    isCurrentTranslation = false,
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Delete translation").assertIsDisplayed()
    }

    @Test
    fun testTranslationRow_isLoading_showsLoadingIndicator() {
        composeTestRule.setContent {
            VerslyTheme {
                TranslationManagementRow(
                    translation = translation(isDownloaded = false),
                    isLoading = true,
                    isCurrentTranslation = false,
                )
            }
        }

        composeTestRule.waitForIdle()
        // When loading, neither download nor delete buttons should be present
        composeTestRule.onNodeWithContentDescription("Download translation").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Delete translation").assertDoesNotExist()
    }

    // endregion

    // region interactions

    @Test
    fun testTranslationRow_downloadButton_invokesCallback() {
        var downloaded = false

        composeTestRule.setContent {
            VerslyTheme {
                TranslationManagementRow(
                    translation = translation(isDownloaded = false),
                    isLoading = false,
                    isCurrentTranslation = false,
                    onDownload = { downloaded = true },
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Download translation").performClick()
        assertTrue(downloaded)
    }

    @Test
    fun testTranslationRow_deleteButton_invokesCallback() {
        var deleted = false

        composeTestRule.setContent {
            VerslyTheme {
                TranslationManagementRow(
                    translation = translation(isDownloaded = true),
                    isLoading = false,
                    isCurrentTranslation = false,
                    onDelete = { deleted = true },
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Delete translation").performClick()
        assertTrue(deleted)
    }

    // endregion
}
