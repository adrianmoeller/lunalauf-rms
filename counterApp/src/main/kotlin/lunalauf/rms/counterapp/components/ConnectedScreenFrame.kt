package lunalauf.rms.counterapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import compose.icons.EvaIcons
import compose.icons.evaicons.Outline
import compose.icons.evaicons.outline.Loader
import compose.icons.evaicons.outline.Wifi
import compose.icons.evaicons.outline.WifiOff
import lunalauf.rms.counterapp.components.commons.cond
import lunalauf.rms.utilities.network.client.Connection
import java.awt.Cursor
import java.awt.Point
import java.awt.Toolkit
import java.awt.image.BufferedImage

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ConnectedScreenFrame(
    modifier: Modifier = Modifier,
    title: String,
    screenModel: MainScreenModel,
    connection: Connection,
    content: @Composable () -> Unit
) {
    val clientStatus by connection.status.collectAsState()
    val ping by connection.ping.collectAsState()
    val connectionStatus = convertStatus(clientStatus)
    var showToolBar by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .padding(10.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onPointerEvent(
                    eventType = PointerEventType.Enter,
                    onEvent = { showToolBar = true }
                )
                .onPointerEvent(
                    eventType = PointerEventType.Exit,
                    onEvent = { showToolBar = false }
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FilledTonalButton(
                modifier = Modifier
                    .heightIn(max = 30.dp)
                    .cond(!showToolBar) {
                        alpha(0f)
                    },
                enabled = showToolBar,
                onClick = { screenModel.disconnect(connection) },
            ) {
                Text(
                    text = "Disconnect",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (screenModel.reconnecting) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Reconnecting")
                    LinearProgressIndicator()
                }
            } else {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (ping >= 0)
                    Text("($ping ms)")
                Icon(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(connectionStatus.color)
                        .padding(4.dp)
                        .size(20.dp),
                    imageVector = connectionStatus.icon,
                    contentDescription = connectionStatus.description
                )
            }
        }
        Box(
            modifier = Modifier.pointerHoverIcon(PointerIcon(createEmptyCursor())),
        ) {
            content()
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

private fun createEmptyCursor(): Cursor {
    return Toolkit.getDefaultToolkit().createCustomCursor(
        BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB),
        Point(0, 0),
        "Empty Cursor"
    )
}