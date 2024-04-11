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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import lunalauf.rms.counterapp.components.*
import lunalauf.rms.utilities.logging.Logger
import lunalauf.rms.utilities.logging.configureStartUpErrorLogging
import java.awt.Color

@Composable
@Preview
fun ApplicationScope.App() {
    val colorScheme = darkColorScheme()
    val icon = painterResource("icons/icon.png")

    Window(
        onCloseRequest = ::exitApplication,
        title = "Luna-Lauf",
        icon = icon
    ) {
        window.background = Color.BLACK

        MaterialTheme(
            colorScheme = colorScheme
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
            ) {
                val screenModel = remember { MainScreenModel() }
                val connectionStatus by screenModel.connectionStatus.collectAsState()

                when (val constConnStatus = connectionStatus) {
                    ConnectionStatus.Disconnected -> {
                        StartScreen(
                            modifier = Modifier.fillMaxSize(),
                            screenModel = screenModel
                        )
                    }

                    is ConnectionStatus.ConnectedRC -> {
                        RoundCounterScreen(
                            modifier = Modifier.fillMaxSize(),
                            roundCounter = constConnStatus.roundCounter
                        )
                    }

                    is ConnectionStatus.ConnectedID -> {
                        InfoDisplayScreen(
                            modifier = Modifier.fillMaxSize(),
                            infoDisplay = constConnStatus.infoDisplay
                        )
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
