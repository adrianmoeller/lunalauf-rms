package lunalauf.rms.centralapp.components.features

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ExclamationTriangle
import compose.icons.fontawesomeicons.solid.Play
import compose.icons.fontawesomeicons.solid.Stop
import lunalauf.rms.centralapp.components.commons.IconSize
import lunalauf.rms.centralapp.components.commons.OptionTile
import lunalauf.rms.centralapp.components.commons.cond
import lunalauf.rms.utilities.network.bot.BotManager
import lunalauf.rms.utilities.network.bot.BotState

@Composable
fun BotSheetScreen(
    modifier: Modifier = Modifier,
    botManager: BotManager
) {
    if (botManager is BotManager.Available) {
        val roundCounterBotState by botManager.roundCounterBotState.collectAsState()
        val runnerInfoBotState by botManager.runnerInfoBotState.collectAsState()
        val silentStart by botManager.silentStart.collectAsState()
        val loadConnections by botManager.loadConnections.collectAsState()

        Column(
            modifier = modifier
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                BotCard(
                    title = "Round Counter Bot",
                    botState = roundCounterBotState.toUIState(),
                    onBotClick = botManager::switchStateRoundCounterBot
                )
                Spacer(Modifier.height(10.dp))
                BotCard(
                    title = "Runner Info Bot",
                    botState = runnerInfoBotState.toUIState(),
                    onBotClick = botManager::switchStateRunnerInfoBot
                )
            }
            Card {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 15.dp,
                        vertical = 10.dp
                    )
                ) {
                    OptionTile(
                        text = "Silent start",
                        checked = silentStart,
                        onCheckedChange = botManager::silentStart
                    )
                    OptionTile(
                        text = "Load connection data",
                        checked = loadConnections,
                        onCheckedChange = botManager::loadConnections
                    )
                }
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
                if (botManager is BotManager.InitializationError) {
                    Text(
                        text = botManager.message,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun BotCard(
    modifier: Modifier = Modifier,
    title: String,
    botState: BotUIState,
    onBotClick: () -> Unit
) {
    OutlinedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(20.dp))
            OutlinedButton(
                onClick = onBotClick,
                enabled = !botState.processing
            ) {
                if (!botState.processing) {
                    Icon(
                        modifier = Modifier.size(15.dp),
                        imageVector = when (botState) {
                            BotUIState.STOPPED -> FontAwesomeIcons.Solid.Play
                            BotUIState.RUNNING -> FontAwesomeIcons.Solid.Stop
                            else -> FontAwesomeIcons.Solid.ExclamationTriangle
                        },
                        contentDescription = null
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Text(text = botState.buttonText)
            }
            LinearProgressIndicator(
                modifier = Modifier
                    .padding(5.dp)
                    .cond(!botState.processing) {
                        alpha(0f)
                    }
            )
        }
    }
}

private fun BotState.toUIState(): BotUIState {
    return when (this) {
        BotState.STOPPED -> BotUIState.STOPPED
        BotState.INITIALIZING -> BotUIState.INITIALIZING
        BotState.RUNNING -> BotUIState.RUNNING
        BotState.TERMINATING -> BotUIState.TERMINATING
    }
}

enum class BotUIState(
    val buttonText: String,
    val processing: Boolean
) {
    STOPPED(
        buttonText = "Start",
        processing = false
    ),
    INITIALIZING(
        buttonText = "Initializing",
        processing = true
    ),
    RUNNING(
        buttonText = "Stop",
        processing = false
    ),
    TERMINATING(
        buttonText = "Terminating",
        processing = true
    )
}