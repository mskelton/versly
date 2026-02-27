package dev.mskelton.versly

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface TopLevelRoute : NavKey {
    val icon: Int
    val label: Int
}

sealed interface SheetRoute : NavKey

@Serializable
data object Read : TopLevelRoute {
    override val label: Int = R.string.read
    override val icon: Int = R.drawable.book_2_24px
}

@Serializable
data object Plans : TopLevelRoute {
    override val label: Int = R.string.plans
    override val icon: Int = R.drawable.library_add_check_24px
}

@Serializable
data object Search : TopLevelRoute {
    override val label: Int = R.string.search
    override val icon: Int = R.drawable.search_24px
}

@Serializable
data object Settings : TopLevelRoute {
    override val label: Int = R.string.settings
    override val icon: Int = R.drawable.settings_24px
}

@Serializable data class PickPassage(
    val current: PassageId,
) : NavKey

@Serializable data object PickTranslationSheet : SheetRoute
