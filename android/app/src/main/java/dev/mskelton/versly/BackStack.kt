package dev.mskelton.versly

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

fun NavBackStack<NavKey>.replace(route: NavKey) {
    clear()
    add(route)
}
