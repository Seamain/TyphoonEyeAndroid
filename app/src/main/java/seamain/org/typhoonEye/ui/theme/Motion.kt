package seamain.org.typhoonEye.ui.theme

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

/**
 * Material 3 motion tokens (motion.easing / motion.duration) —
 * standard Android-native transition patterns:
 * - Shared axis X: forward/backward navigation (Home <-> Detail, tab order)
 * - Fade through: swapping unrelated content (loading -> list)
 */
object Motion {
    // M3 easing tokens
    val Emphasized = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
    val Standard = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    // M3 duration tokens
    const val DurationShort4 = 200
    const val DurationMedium2 = 300
    const val DurationMedium4 = 400

    private const val SharedAxisOffset = 30 // dp-ish px fraction basis

    /** Shared axis X, forward (navigate deeper / next tab). */
    fun sharedAxisXForward(): ContentTransform =
        (slideInHorizontally(
            animationSpec = tween(DurationMedium2, easing = EmphasizedDecelerate)
        ) { it / 10 } + fadeIn(
            animationSpec = tween(DurationMedium2, delayMillis = 60, easing = Standard)
        )) togetherWith (slideOutHorizontally(
            animationSpec = tween(DurationShort4, easing = EmphasizedAccelerate)
        ) { -it / 10 } + fadeOut(
            animationSpec = tween(DurationShort4, easing = Standard)
        ))

    /** Shared axis X, backward (navigate up / previous tab). */
    fun sharedAxisXBackward(): ContentTransform =
        (slideInHorizontally(
            animationSpec = tween(DurationMedium2, easing = EmphasizedDecelerate)
        ) { -it / 10 } + fadeIn(
            animationSpec = tween(DurationMedium2, delayMillis = 60, easing = Standard)
        )) togetherWith (slideOutHorizontally(
            animationSpec = tween(DurationShort4, easing = EmphasizedAccelerate)
        ) { it / 10 } + fadeOut(
            animationSpec = tween(DurationShort4, easing = Standard)
        ))

    /** Fade through — content swap with no spatial relationship (M3 pattern). */
    fun fadeThrough(): ContentTransform =
        (fadeIn(
            animationSpec = tween(DurationMedium2, delayMillis = 90, easing = EmphasizedDecelerate)
        ) + scaleIn(
            initialScale = 0.92f,
            animationSpec = tween(DurationMedium2, delayMillis = 90, easing = EmphasizedDecelerate)
        )) togetherWith fadeOut(
            animationSpec = tween(90, easing = EmphasizedAccelerate)
        )

    fun fadeEnter() = fadeIn(animationSpec = tween(DurationMedium2, easing = EmphasizedDecelerate))
    fun fadeExit() = fadeOut(animationSpec = tween(DurationShort4, easing = EmphasizedAccelerate))
}
