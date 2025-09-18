package dev.mskelton.versly

enum class AppDestination(val label: Int, val icon: Int, val contentDescription: Int) {
    READ(R.string.read, R.drawable.book_2_24px, R.string.read),
    PLANS(R.string.plans, R.drawable.library_add_check_24px, R.string.plans),
    SEARCH(R.string.search, R.drawable.search_24px, R.string.search),
}
