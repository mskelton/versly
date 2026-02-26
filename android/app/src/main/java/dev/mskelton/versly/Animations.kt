package dev.mskelton.versly

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset

private const val DURATION = 200
private const val OFFSET_FRACTION = 0.25f

fun horizontalSlideTransition(direction: Int): ContentTransform {
    val spec = tween<IntOffset>(durationMillis = DURATION, easing = FastOutSlowInEasing)
    val fadeSpec = tween<Float>(durationMillis = DURATION, easing = FastOutSlowInEasing)

    return slideInHorizontally(spec, initialOffsetX = { (it * OFFSET_FRACTION * direction).toInt() }) +
        fadeIn(fadeSpec) togetherWith
        slideOutHorizontally(spec, targetOffsetX = { (-it * OFFSET_FRACTION * direction).toInt() }) +
        fadeOut(fadeSpec)
}

fun crossfadeTransition(): ContentTransform {
    val fadeSpec = tween<Float>(durationMillis = 500, easing = FastOutSlowInEasing)
    return fadeIn(fadeSpec) togetherWith fadeOut(fadeSpec)
}

fun horizontalSlidePopTransition(direction: Int): ContentTransform {
    val spec = tween<IntOffset>(durationMillis = DURATION, easing = FastOutSlowInEasing)
    val fadeSpec = tween<Float>(durationMillis = DURATION, easing = FastOutSlowInEasing)

    return slideInHorizontally(spec, initialOffsetX = { (-it * OFFSET_FRACTION * direction).toInt() }) +
        fadeIn(fadeSpec) togetherWith
        slideOutHorizontally(spec, targetOffsetX = { (it * OFFSET_FRACTION * direction).toInt() }) +
        fadeOut(fadeSpec)
}
