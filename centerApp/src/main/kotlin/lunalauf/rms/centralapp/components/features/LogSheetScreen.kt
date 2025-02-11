package lunalauf.rms.centralapp.components.features

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.*
import lunalauf.rms.centralapp.components.commons.IconSize
import lunalauf.rms.centralapp.components.commons.ListItemDivider
import lunalauf.rms.centralapp.components.commons.cond
import lunalauf.rms.centralapp.components.commons.listItemHoverColor
import lunalauf.rms.model.helper.Formats
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
        LevelSelector(
            modifier = Modifier.padding(5.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(logMessages) { index, logMessage ->
                LogMessageTile(
                    level = logMessage.level,
                    message = logMessage.message,
                    logger = logMessage.logger,
                    timestamp = Formats.timeFormat.format(logMessage.timestamp)
                )
                if (index < logMessages.lastIndex)
                    ListItemDivider(spacing = 6.dp)
            }
        }
    }
}

@Composable
private fun LevelSelector(
    modifier: Modifier = Modifier
) {
    var selectorOpen by remember { mutableStateOf(false) }
    val logLevel by Logger.currentLogLevel.collectAsState()

    Card(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text("Log level:")
                Text(
                    text = logLevel.name,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Box {
                IconButton(
                    onClick = { selectorOpen = true }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Select log level"
                    )
                }
                DropdownMenu(
                    expanded = selectorOpen,
                    onDismissRequest = { selectorOpen = false },
                    offset = DpOffset(x = (-102).dp, y = (-4).dp)
                ) {
                    Lvl.entries.forEach {
                        DropdownMenuItem(
                            text = { Text(it.name) },
                            leadingIcon = { it.getIcon(IconSize.small) },
                            trailingIcon = {
                                if (it == logLevel)
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null
                                    )
                            },
                            onClick = {
                                Logger.setLevel(it)
                                selectorOpen = false
                            },
                        )
                    }
                }
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
        Lvl.ALL -> Icon(
            modifier = Modifier.size(iconSize),
            imageVector = FontAwesomeIcons.Solid.CheckCircle,
            contentDescription = this.name
        )

        Lvl.DEBUG -> Icon(
            modifier = Modifier.size(iconSize),
            imageVector = FontAwesomeIcons.Solid.Bug,
            contentDescription = this.name
        )

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

        Lvl.FATAL -> Icon(
            modifier = Modifier.size(iconSize),
            imageVector = FontAwesomeIcons.Solid.TimesCircle,
            contentDescription = this.name
        )
    }
}