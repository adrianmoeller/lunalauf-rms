package lunalauf.rms.counterapp

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.*
import lunalauf.rms.counterapp.components.*
import lunalauf.rms.utilities.logging.Logger
import lunalauf.rms.utilities.logging.configureStartUpErrorLogging
import java.awt.Color

@Composable
@Preview
fun ApplicationScope.App() {
    val colorScheme = darkColorScheme()
    val icon = painterResource("icons/icon.png")

    val screenModel = remember { MainScreenModel() }
    val connectionStatus by screenModel.connectionStatus.collectAsState()

    val startWindowState = rememberWindowState()
    val connectedWindowState = rememberWindowState()

    Window(
        onCloseRequest = ::exitApplication,
        title = "Luna-Lauf",
        icon = icon,
        state = if (connectionStatus == MainConnectionStatus.Disconnected) startWindowState
            else connectedWindowState,
        onKeyEvent = {
            if (connectionStatus != MainConnectionStatus.Disconnected && it.key == Key.F && it.type == KeyEventType.KeyDown) {
                if (connectedWindowState.placement != WindowPlacement.Fullscreen)
                    connectedWindowState.placement = WindowPlacement.Fullscreen
                else
                    connectedWindowState.placement = WindowPlacement.Floating
                return@Window true
            }
            return@Window false
        }
    ) {
        window.background = Color.BLACK

        MaterialTheme(
            colorScheme = colorScheme
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
            ) {
                when (val constConnStatus = connectionStatus) {
                    MainConnectionStatus.Disconnected -> {
                        StartScreen(
                            modifier = Modifier.fillMaxSize(),
                            screenModel = screenModel
                        )
                    }

                    is MainConnectionStatus.ConnectedRC -> {
                        ConnectedScreenFrame(
                            title = "Scan rounds:",
                            screenModel = screenModel,
                            client = constConnStatus.roundCounter.client,
                        ) {
                            RoundCounterScreen(
                                roundCounter = constConnStatus.roundCounter
                            )
                        }
                    }

                    is MainConnectionStatus.ConnectedID -> {
                        ConnectedScreenFrame(
                            title = "Show info:",
                            screenModel = screenModel,
                            client = constConnStatus.infoDisplay.client
                        ) {
                            InfoDisplayScreen(
                                infoDisplay = constConnStatus.infoDisplay
                            )
                        }
                    }
                }
            }
        }
    }
}

fun main() {
    configureStartUpErrorLogging()
    Logger.configure()

    application {
        App()
    }
}
