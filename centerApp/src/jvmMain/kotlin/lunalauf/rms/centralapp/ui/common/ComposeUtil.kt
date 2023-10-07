package lunalauf.rms.centralapp.ui.common

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

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
    Divider(
        modifier = modifier.padding(horizontal = spacing),
        color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.09f)
    )
}