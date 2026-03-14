package dev.mskelton.versly.testutil

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.File
import java.io.FileOutputStream

abstract class ComposeScreenshotTest {
    val composeTestRule = createComposeRule()
    private val screenshotWatcher = ScreenshotWatcher()

    @get:Rule
    val ruleChain: TestRule = RuleChain.outerRule(composeTestRule).around(screenshotWatcher)

    @Suppress("unused")
    fun takeScreenshot(name: String) {
        screenshotWatcher.takeScreenshot(name)
    }

    private inner class ScreenshotWatcher : TestWatcher() {
        override fun failed(
            e: Throwable?,
            description: Description?,
        ) {
            takeScreenshot("${description?.className}_${description?.methodName}")
        }

        fun takeScreenshot(name: String) {
            try {
                composeTestRule.waitForIdle()
                val bitmap = composeTestRule.onRoot().captureToImage().asAndroidBitmap()
                saveScreenshot(bitmap, name)
            } catch (e: Exception) {
                println("Failed to capture screenshot: ${e.message}")
            }
        }

        private fun saveScreenshot(
            bitmap: Bitmap,
            name: String,
        ) {
            val outputDir =
                InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
                    ?: return

            val screenshotsDir = File(outputDir)
            screenshotsDir.mkdirs()

            val file = File(screenshotsDir, "$name.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            println("Screenshot saved: ${file.absolutePath}")
        }
    }
}
