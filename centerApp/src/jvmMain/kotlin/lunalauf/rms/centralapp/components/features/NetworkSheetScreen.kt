package lunalauf.rms.centralapp.components.features

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import compose.icons.EvaIcons
import compose.icons.FontAwesomeIcons
import compose.icons.evaicons.Outline
import compose.icons.evaicons.outline.Loader
import compose.icons.evaicons.outline.Wifi
import compose.icons.evaicons.outline.WifiOff
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ExclamationTriangle
import compose.icons.fontawesomeicons.solid.Search
import compose.icons.fontawesomeicons.solid.Stop
import lunalauf.rms.centralapp.components.commons.IconSize
import lunalauf.rms.centralapp.components.commons.ListItemDivider
import lunalauf.rms.centralapp.components.commons.cond
import lunalauf.rms.centralapp.components.commons.listItemHoverColor
import lunalauf.rms.utilities.network.server.NetworkManager
import lunalauf.rms.utilities.network.util.Service

@Composable
fun NetworkSheetScreen(
    modifier: Modifier = Modifier,
    networkManager: NetworkManager
) {
    if (networkManager is NetworkManager.Available) {
        val connections by networkManager.clientHandler.clients.collectAsState()
        val catcherState by networkManager.clientCatcher.state.collectAsState()

        Column(
            modifier = modifier.fillMaxHeight(),
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
                        text = "Port: ${networkManager.port}"
                    )
                }
                LazyColumn(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .fillMaxWidth()
                ) {
                    itemsIndexed(connections) { index, client ->
                        val status by client.status.collectAsState()
                        ConnectionTile(
                            connectionStatus = convertStatus(status),
                            ipAddress = client.remoteAddress,
                            onRemove = {networkManager.clientHandler.removeClient(client)}
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
                    onClick = {
                        when (catcherState) {
                            Service.State.Running -> networkManager.clientCatcher.stop()
                            Service.State.Idling -> networkManager.clientCatcher.start(Unit)
                            Service.State.Transitioning -> {}
                        }
                    },
                    enabled = catcherState != Service.State.Transitioning
                ) {
                    Icon(
                        modifier = Modifier.size(15.dp),
                        imageVector = if (catcherState == Service.State.Running) FontAwesomeIcons.Solid.Stop
                        else FontAwesomeIcons.Solid.Search,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(text = if (catcherState == Service.State.Running) "Stop searching" else "Search for clients")
                }
                LinearProgressIndicator(
                    modifier = Modifier
                        .padding(vertical = 5.dp)
                        .cond(catcherState == Service.State.Idling) {
                            alpha(0f)
                        }
                )
            }
        }
    } else {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    modifier = Modifier.size(IconSize.extraLarge),
                    imageVector = FontAwesomeIcons.Solid.ExclamationTriangle,
                    contentDescription = null
                )
                if (networkManager is NetworkManager.InitializationError) {
                    Text(
                        text = networkManager.message,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ConnectionTile(
    modifier: Modifier = Modifier,
    connectionStatus: ConnectionStatus,
    ipAddress: String,
    spacing: Dp = 15.dp,
    onRemove: () -> Unit
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
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = modifier
                .padding(vertical = spacing)
                .padding(start = spacing)
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
        if (hovered && connectionStatus == ConnectionStatus.DISCONNECTED) {
            Row {
                IconButton(
                    onClick = onRemove
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Remove"
                    )
                }
                Spacer(Modifier.width(5.dp))
            }
        }
    }
}

private fun convertStatus(status: Int): ConnectionStatus {
    return when (status) {
        1 -> ConnectionStatus.CONNECTED
        0 -> ConnectionStatus.WAITING
        -1 -> ConnectionStatus.DISCONNECTED
        else -> ConnectionStatus.DISCONNECTED
    }
}

private enum class ConnectionStatus(
    val icon: ImageVector,
    val color: Color,
    val description: String
) {
    CONNECTED(
        icon = EvaIcons.Outline.Wifi,
        color = Color(0xFF21B14D),
        description = "Connected"
    ),
    WAITING(
        icon = EvaIcons.Outline.Loader,
        color = Color(0xFFF08C2D),
        description = "Waiting"
    ),
    DISCONNECTED(
        icon = EvaIcons.Outline.WifiOff,
        color = Color(0xFFDA2955),
        description = "Disconnected"
    )
}