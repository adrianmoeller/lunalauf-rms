package lunalauf.rms.counterapp.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import lunalauf.rms.counterapp.components.commons.cond
import lunalauf.rms.utilities.network.util.Service

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreen(
    modifier: Modifier = Modifier,
    screenModel: MainScreenModel
) {
    val connectorState by screenModel.connectorState.collectAsState()
    val counterType by screenModel.counterType.collectAsState()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Text(
            text = "Counter",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        SingleChoiceSegmentedButtonRow {
            SegmentedButton(
                modifier = Modifier.width(IntrinsicSize.Max),
                selected = counterType == CounterType.RoundCounter,
                onClick = { screenModel.updateCounterType(CounterType.RoundCounter) },
                label = { Text("Scan rounds") },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            )
            SegmentedButton(
                modifier = Modifier.width(IntrinsicSize.Max),
                selected = counterType == CounterType.InfoDisplay,
                onClick = { screenModel.updateCounterType(CounterType.InfoDisplay) },
                label = { Text("Show info") },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = screenModel.port,
                onValueChange = screenModel::updatePort,
                label = { Text("Port") },
            )
            OutlinedTextField(
                value = screenModel.host,
                onValueChange = screenModel::updateHost,
                label = { Text("Host") },
            )
            Spacer(Modifier.height(10.dp))
            FilledTonalButton(
                onClick = {
                    when (connectorState) {
                        Service.State.Idling -> screenModel.startConnector()
                        Service.State.Running -> screenModel.stopConnector()
                        else -> {}
                    }
                },
                enabled = connectorState != Service.State.Transitioning
            ) {
                Text(
                    when (connectorState) {
                        Service.State.Idling -> "Connect to server"
                        Service.State.Running, Service.State.Transitioning -> "Cancel"
                    }
                )
            }
            LinearProgressIndicator(
                modifier = Modifier
                    .padding(vertical = 5.dp)
                    .cond(connectorState == Service.State.Idling) {
                        alpha(0f)
                    }
            )
        }
    }
}