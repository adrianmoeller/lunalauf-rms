package lunalauf.rms.centralapp.ui.components.features

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ExclamationTriangle
import compose.icons.fontawesomeicons.solid.Play
import compose.icons.fontawesomeicons.solid.Stop
import lunalauf.rms.centralapp.ui.components.commons.OptionTile
import lunalauf.rms.centralapp.ui.components.commons.cond

@Composable
fun BotSheetContent(
    modifier: Modifier = Modifier,
    roundCounterBotStatus: BotStatus,
    onRoundCounterBotClick: () -> Unit,
    runnerInfoBotStatus: BotStatus,
    onRunnerInfoBotClick: () -> Unit,
    silentStart: Boolean,
    onSilentStartClick: (Boolean) -> Unit,
    loadConnectionData: Boolean,
    onLoadConnectionDataClick: (Boolean) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            BotCard(
                title = "Round Counter Bot",
                botStatus = roundCounterBotStatus,
                onBotClick = onRoundCounterBotClick
            )
            Spacer(Modifier.height(10.dp))
            BotCard(
                title = "Runner Info Bot",
                botStatus = runnerInfoBotStatus,
                onBotClick = onRunnerInfoBotClick
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
                    onCheckedChange = onSilentStartClick
                )
                OptionTile(
                    text = "Load connection data",
                    checked = loadConnectionData,
                    onCheckedChange = onLoadConnectionDataClick
                )
            }
        }
    }
}

@Composable
fun BotCard(
    modifier: Modifier = Modifier,
    title: String,
    botStatus: BotStatus,
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
                enabled = !botStatus.progressing
            ) {
                if (!botStatus.progressing) {
                    Icon(
                        modifier = Modifier.size(15.dp),
                        imageVector = when (botStatus) {
                            BotStatus.STOPPED -> FontAwesomeIcons.Solid.Play
                            BotStatus.RUNNING -> FontAwesomeIcons.Solid.Stop
                            else -> FontAwesomeIcons.Solid.ExclamationTriangle
                        },
                        contentDescription = null
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Text(text = botStatus.buttonText)
            }
            LinearProgressIndicator(
                modifier = Modifier
                    .padding(5.dp)
                    .cond(!botStatus.progressing) {
                        alpha(0f)
                    }
            )
        }
    }
}

enum class BotStatus(
    val buttonText: String,
    val progressing: Boolean
) {
    STOPPED(
        buttonText = "Start",
        progressing = false
    ),
    INITIALIZING(
        buttonText = "Initializing",
        progressing = true
    ),
    RUNNING(
        buttonText = "Stop",
        progressing = false
    ),
    TERMINATING(
        buttonText = "Terminating",
        progressing = true
    )
}