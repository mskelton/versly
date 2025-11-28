package dev.mskelton.versly

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

class SheetScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    val contentEntry: NavEntry<T>,
    val sheetEntry: NavEntry<T>?,
) : Scene<T> {
    override val entries: List<NavEntry<T>> =
        if (sheetEntry != null) {
            listOf(contentEntry, sheetEntry)
        } else {
            listOf(contentEntry)
        }

    override val content: @Composable (() -> Unit) = {
        Box(modifier = Modifier.fillMaxSize()) {
            contentEntry.Content()
            sheetEntry?.Content()
        }
    }
}

class SheetSceneStrategy<T : Any> : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        if (entries.isEmpty()) return null

        val contentEntry = entries.findLast { !it.metadata.containsKey(SHEET_KEY) } ?: return null
        val sheetEntry = entries.findLast { it.metadata.containsKey(SHEET_KEY) }

        // We use the list's contentKey to uniquely identify the scene. This prevents animating
        // the content when the sheet is opened or closed.
        val sceneKey = contentEntry.contentKey

        return SheetScene(
            key = sceneKey,
            previousEntries = entries.dropLast(1),
            contentEntry = contentEntry,
            sheetEntry = sheetEntry,
        )
    }

    companion object {
        internal const val INDEX_KEY = "SheetScene-Index"
        internal const val SHEET_KEY = "SheetScene-Sheet"

        /**
         * Helper function to add metadata to a [NavEntry] indicating it can be displayed as a sheet
         * in the [SheetScene].
         */
        fun sheet() = mapOf(SHEET_KEY to true)

        /*
         * Helper function to add metadata to a [NavEntry] indicating its index in the tab list.
         */
        fun index(index: Int) = mapOf(INDEX_KEY to index)
    }
}

@Composable
fun <T : Any> rememberSheetSceneStrategy(): SheetSceneStrategy<T> {
    return remember { SheetSceneStrategy() }
}

fun NavBackStack<NavKey>.addSheet(sheetRoute: SheetRoute) {
    // Remove any existing sheets, then add the new sheet
    removeIf { it is SheetRoute }
    add(sheetRoute)
}
