package lunalauf.rms.centralapp.components.features

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import compose.icons.EvaIcons
import compose.icons.FontAwesomeIcons
import compose.icons.evaicons.Outline
import compose.icons.evaicons.outline.Loader
import compose.icons.evaicons.outline.Wifi
import compose.icons.evaicons.outline.WifiOff
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Search
import compose.icons.fontawesomeicons.solid.Stop
import lunalauf.rms.centralapp.components.commons.ListItemDivider
import lunalauf.rms.centralapp.components.commons.cond
import lunalauf.rms.centralapp.components.commons.listItemHoverColor

@Composable
fun NetworkSheetContent(
    modifier: Modifier = Modifier,
    port: Int,
    searching: Boolean,
    onSearchClick: () -> Unit,
    connections: List<Pair<ConnectionStatus, String>>
) {
    Column(
        modifier = modifier
            .fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.CenterHorizontally),
                    text = "Port: $port"
                )
            }
            LazyColumn(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .fillMaxWidth()
            ) {
                itemsIndexed(connections) {index, item ->
                    ConnectionTile(
                        connectionStatus = item.first,
                        ipAddress = item.second,
                        spacing = 10.dp
                    )
                    if (index < connections.lastIndex)
                        ListItemDivider(spacing = 10.dp)
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedButton(
                onClick = onSearchClick
            ) {
                Icon(
                    modifier = Modifier.size(15.dp),
                    imageVector = if (searching) FontAwesomeIcons.Solid.Stop else FontAwesomeIcons.Solid.Search,
                    contentDescription = null
                )
                Spacer(Modifier.width(10.dp))
                Text(text = if (searching) "Stop searching" else "Search for clients")
            }
            LinearProgressIndicator(
                modifier = Modifier
                    .padding(vertical = 5.dp)
                    .cond(!searching) {
                        alpha(0f)
                    }
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ConnectionTile(
    modifier: Modifier = Modifier,
    connectionStatus: ConnectionStatus,
    ipAddress: String,
    spacing: Dp
) {
    var hovered by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .onPointerEvent(PointerEventType.Enter) { hovered = true }
            .onPointerEvent(PointerEventType.Exit) { hovered = false }
            .cond(hovered) {
                background(
                    color = listItemHoverColor,
                    shape = MaterialTheme.shapes.medium
                )
            }
            .padding(spacing)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .clip(CircleShape)
                .background(connectionStatus.color)
                .padding(4.dp)
                .size(20.dp),
            imageVector = connectionStatus.icon,
            contentDescription = connectionStatus.description
        )
        Spacer(Modifier.width(15.dp))
        Text(
            text = ipAddress
        )
    }
}

enum class ConnectionStatus(
    val icon: ImageVector,
    val color: Color,
    val description: String
) {
    CONNECTED(
        EvaIcons.Outline.Wifi,
        Color(0xFF21B14D),
        "Connected"
    ),
    WAITING(
        EvaIcons.Outline.Loader,
        Color(0xFFF08C2D),
        "Waiting"
    ),
    DISCONNECTED(
        EvaIcons.Outline.WifiOff,
        Color(0xFFDA2955),
        "Disconnected"
    )
}