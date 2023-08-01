package dev.shushant.deviceauthenticationsample.presentation.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.wear.compose.material.MaterialTheme
import dev.shushant.deviceauthenticationsample.presentation.AmbientState
import java.time.Instant
import kotlin.random.Random

const val BURN_IN_OFFSET_PX = 10


@Composable
fun DeviceAuthenticationSampleTheme(
    content: @Composable () -> Unit
) {
    /**
     * Empty theme to customize for your app.
     * See: https://developer.android.com/jetpack/compose/designsystems/custom
     */
    MaterialTheme(
        content = content
    )
}

fun Modifier.burnInTranslation(
    ambientState: AmbientState,
    ambientUpdateTimestamp: Instant
): Modifier = composed {
    val translationX = rememberBurnInTranslation(ambientState, ambientUpdateTimestamp)
    val translationY = rememberBurnInTranslation(ambientState, ambientUpdateTimestamp)

    this
        .graphicsLayer {
            this.translationX = translationX
            this.translationY = translationY
        }
}

@Composable
fun rememberBurnInTranslation(
    ambientState: AmbientState,
    ambientUpdateTimestamp: Instant
): Float =
    remember(ambientState, ambientUpdateTimestamp) {
        when (ambientState) {
            AmbientState.Interactive -> 0f
            is AmbientState.Ambient -> if (ambientState.doBurnInProtection) {
                Random.nextInt(-BURN_IN_OFFSET_PX, BURN_IN_OFFSET_PX + 1).toFloat()
            } else {
                0f
            }
        }
    }

private tailrec fun Context.findActivity(): Activity =
    when (this) {
        is Activity -> this
        is ContextWrapper -> this.baseContext.findActivity()
        else -> throw IllegalStateException(
            "findActivity must be called in the context of an activity!"
        )
    }