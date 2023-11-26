package lunalauf.rms.centralapp.components.features

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Bug
import compose.icons.fontawesomeicons.solid.ExclamationCircle
import compose.icons.fontawesomeicons.solid.ExclamationTriangle
import compose.icons.fontawesomeicons.solid.InfoCircle
import lunalauf.rms.centralapp.components.commons.IconSize
import lunalauf.rms.centralapp.components.commons.ListItemDivider
import lunalauf.rms.centralapp.components.commons.cond
import lunalauf.rms.centralapp.components.commons.listItemHoverColor
import lunalauf.rms.centralapp.utils.Formats
import lunalauf.rms.utilities.logging.Logger
import lunalauf.rms.utilities.logging.Lvl

@Composable
fun LogSheetScreen(
    modifier: Modifier = Modifier
) {
    val logMessages by Logger.logMessages.collectAsState()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(logMessages) { index, logMessage ->
                LogMessageTile(
                    level = logMessage.level,
                    message = logMessage.message,
                    logger = logMessage.logger,
                    timestamp = Formats.dayTimeFormat.format(logMessage.timestamp)
                )
                if (index < logMessages.lastIndex)
                    ListItemDivider(spacing = 6.dp)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun LogMessageTile(
    modifier: Modifier = Modifier,
    level: Lvl,
    message: String,
    logger: String,
    timestamp: String
) {
    var hovered by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .onPointerEvent(PointerEventType.Enter) { hovered = true }
            .onPointerEvent(PointerEventType.Exit) { hovered = false }
            .cond(hovered) {
                background(
                    color = listItemHoverColor,
                    shape = MaterialTheme.shapes.small
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 7.dp)
                .padding(end = 5.dp)
                .padding(vertical = 5.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                level.getIcon(IconSize.extraSmall)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = timestamp,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = logger,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f)
                    )
                }
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun Lvl.getIcon(iconSize: Dp) {
    when (this) {
        Lvl.INFO -> Icon(
            modifier = Modifier.size(iconSize),
            imageVector = FontAwesomeIcons.Solid.InfoCircle,
            contentDescription = this.name
        )

        Lvl.WARN -> Icon(
            modifier = Modifier.size(iconSize),
            imageVector = FontAwesomeIcons.Solid.ExclamationTriangle,
            contentDescription = this.name
        )

        Lvl.ERROR -> Icon(
            modifier = Modifier.size(iconSize),
            imageVector = FontAwesomeIcons.Solid.ExclamationCircle,
            contentDescription = this.name
        )

        Lvl.DEBUG -> Icon(
            modifier = Modifier.size(iconSize),
            imageVector = FontAwesomeIcons.Solid.Bug,
            contentDescription = this.name
        )
    }
}