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
import lunalauf.rms.utilities.network.client.InfoDisplay
import lunalauf.rms.utilities.network.client.RoundCounter

@Composable
@Preview
fun ApplicationScope.App() {
    val colorScheme = darkColorScheme()
    val icon = painterResource("icons/icon.png")

    val screenModel = remember { MainScreenModel() }
    val mainState by screenModel.mainState.collectAsState()

    when (val constMainState = mainState) {
        MainState.Start -> {
            Window(
                onCloseRequest = ::exitApplication,
                title = "Luna-Lauf",
                icon = icon
            ) {
                MaterialTheme(
                    colorScheme = colorScheme
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        StartScreen(
                            modifier = Modifier.fillMaxSize(),
                            screenModel = screenModel
                        )
                    }
                }
            }
        }

        is MainState.Operating -> {
            val connectedWindowState = rememberWindowState(WindowPlacement.Maximized)

            Window(
                onCloseRequest = ::exitApplication,
                title = "Luna-Lauf",
                icon = icon,
                state = connectedWindowState,
                undecorated = true,
                onKeyEvent = {
                    if (it.key == Key.F && it.type == KeyEventType.KeyDown) {
                        if (connectedWindowState.placement != WindowPlacement.Maximized)
                            connectedWindowState.placement = WindowPlacement.Maximized
                        else
                            connectedWindowState.placement = WindowPlacement.Floating
                        return@Window true
                    }
                    return@Window false
                }
            ) {
                MaterialTheme(
                    colorScheme = colorScheme
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        when (constMainState.operator) {
                            is RoundCounter -> {
                                ConnectedScreenFrame(
                                    title = "Scan rounds:",
                                    screenModel = screenModel,
                                    connection = constMainState.operator.connection,
                                ) {
                                    RoundCounterScreen(
                                        roundCounter = constMainState.operator
                                    )
                                }
                            }

                            is InfoDisplay -> {
                                ConnectedScreenFrame(
                                    title = "Show info:",
                                    screenModel = screenModel,
                                    connection = constMainState.operator.connection
                                ) {
                                    InfoDisplayScreen(
                                        infoDisplay = constMainState.operator
                                    )
                                }
                            }

                            else -> {}
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
