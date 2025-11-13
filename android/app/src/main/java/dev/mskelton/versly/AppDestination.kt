package dev.mskelton.versly

enum class AppDestination(val label: Int, val icon: Int) {
    READ(R.string.read, R.drawable.book_2_24px),
    PLANS(R.string.plans, R.drawable.library_add_check_24px),
    SEARCH(R.string.search, R.drawable.search_24px),
}
