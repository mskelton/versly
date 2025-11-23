package dev.mskelton.versly

import androidx.navigation3.runtime.NavKey
import dev.mskelton.versly.persistence.PassageId
import kotlinx.serialization.Serializable

sealed interface TopLevelRoute : NavKey {
    val icon: Int
    val label: Int
}

@Serializable
data class Read(val passageId: PassageId? = null) : TopLevelRoute {
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
