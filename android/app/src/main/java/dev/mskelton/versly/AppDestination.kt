package dev.mskelton.versly

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

enum class AppDestination(
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    @DrawableRes val iconSelected: Int,
    @StringRes val contentDescription: Int
) {
    READ(
        R.string.read,
        R.drawable.book_2_24px,
        R.drawable.book_2_24px_filled,
        R.string.read
    ),
    PLANS(
        R.string.plans,
        R.drawable.library_add_check_24px,
        R.drawable.library_add_check_24px_filled,
        R.string.plans
    ),
    PROFILE(
        R.string.profile, R.drawable.person_24px, R.drawable.person_24px_filled, R.string.profile
    ),
}
