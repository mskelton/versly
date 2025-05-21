package dev.mskelton.versly

enum class AppDestination(
    val route: String,
    val label: Int,
    val icon: Int,
    val iconSelected: Int,
    val contentDescription: Int,
) {
    READ(
        "read",
        R.string.read,
        R.drawable.book_2_24px,
        R.drawable.book_2_24px_filled,
        R.string.read,
    ),
    PLANS(
        "plans",
        R.string.plans,
        R.drawable.library_add_check_24px,
        R.drawable.library_add_check_24px_filled,
        R.string.plans,
    ),
    PROFILE(
        "profile",
        R.string.profile,
        R.drawable.person_24px,
        R.drawable.person_24px_filled,
        R.string.profile,
    ),
}
