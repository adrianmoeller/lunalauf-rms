package lunalauf.rms.centralapp.components.commons

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.cond(
    condition: Boolean,
    modifier: @Composable Modifier.() -> Modifier
): Modifier {
    return if (condition)
        then(modifier(Modifier))
    else
        this
}

val customScrollbarStyle
    @Composable get() = LocalScrollbarStyle.current.copy(
        unhoverColor = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.12f),
        hoverColor = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.50f)
    )

val listItemHoverColor
    @Composable get() = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.03f)

@Composable
fun ListItemDivider(
    modifier: Modifier = Modifier,
    spacing: Dp
) {
    HorizontalDivider(
        modifier = modifier.padding(horizontal = spacing),
        color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.09f)
    )
}

object IconSize {
    val extraSmall = 15.dp
    val small = 20.dp
    val medium = 25.dp
    val large = 30.dp
    val extraLarge = 50.dp
}

data class CustomSnackBarVisuals(
    override val actionLabel: String?,
    override val duration: SnackbarDuration,
    override val message: String,
    override val withDismissAction: Boolean,
    val isError: Boolean
) : SnackbarVisuals

suspend fun SnackbarHostState.showSnackbar(
    message: String,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: SnackbarDuration =
        if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Indefinite,
    isError: Boolean = false
): SnackbarResult = showSnackbar(
    CustomSnackBarVisuals(
        message = message,
        actionLabel = actionLabel,
        withDismissAction = withDismissAction,
        duration = duration,
        isError = isError
    )
)

@Composable
fun FocusRequester.tryRequestFocus() {
    LaunchedEffect(true) {
        try {
            requestFocus()
        } catch (_: Exception) {}
    }
}
