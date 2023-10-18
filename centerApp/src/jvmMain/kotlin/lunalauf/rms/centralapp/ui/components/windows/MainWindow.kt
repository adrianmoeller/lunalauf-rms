package lunalauf.rms.centralapp.ui.components.windows

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import kotlinx.coroutines.launch
import lunalauf.rms.centralapp.ui.components.features.*
import java.awt.Color

@Composable
fun ApplicationScope.MainWindow(
    modifier: Modifier = Modifier,
    colorScheme: ColorScheme,
    onPublicViewOpenChange: (Boolean) -> Unit,
    publicViewOpen: Boolean,
    onPublicViewSettingsClick: () -> Unit,
    publicViewAvailable: Boolean,
    content: @Composable () -> Unit
) {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Luna-Lauf"
    ) {
        window.background = Color.BLACK

        MaterialTheme(
            colorScheme = colorScheme
        ) {
            val scope = rememberCoroutineScope()
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            var networkOpen by remember { mutableStateOf(false) }
            var botsOpen by remember { mutableStateOf(false) }
            var logOpen by remember { mutableStateOf(false) }

            ModalNavigationDrawer(
                modifier = modifier,
                drawerState = drawerState,
                gesturesEnabled = true,
                drawerContent = {
                    ModalDrawerSheet {
                        var searching by remember { mutableStateOf(false) }
                        if (networkOpen) {
                            NetworkSheetContent(
                                modifier = Modifier.padding(10.dp),
                                port = 5000,
                                onSearchClick = { searching = !searching },
                                searching = searching,
                                connections = listOf(
                                    Pair(ConnectionStatus.CONNECTED, "1.2.3.4"),
                                    Pair(ConnectionStatus.WAITING, "4.3.2.1"),
                                    Pair(ConnectionStatus.DISCONNECTED, "6.7.8.9")
                                )
                            )
                        }
                        if (botsOpen) {
                            BotSheetContent(
                                modifier = Modifier.padding(10.dp),
                                roundCounterBotStatus = BotStatus.INITIALIZING,
                                onRoundCounterBotClick = {},
                                runnerInfoBotStatus = BotStatus.STOPPED,
                                onRunnerInfoBotClick = {},
                                silentStart = false,
                                onSilentStartClick = {},
                                loadConnectionData = true,
                                onLoadConnectionDataClick = {}
                            )
                        }
                        if (logOpen) {
                            // TODO
                        }
                    }
                }
            ) {
                Row(
                    modifier = Modifier.background(color = MaterialTheme.colorScheme.surface)
                ) {
                    FeatureRail(
                        onPublicViewOpenChange = onPublicViewOpenChange,
                        publicViewOpen = publicViewOpen,
                        onPublicViewSettingsClick = onPublicViewSettingsClick,
                        publicViewAvailable = publicViewAvailable,
                        onNetworkClick = {
                            networkOpen = true
                            botsOpen = false
                            logOpen = false
                            scope.launch { drawerState.open() }
                        },
                        networkOpen = networkOpen && drawerState.isOpen,
                        onBotsClick = {
                            networkOpen = false
                            botsOpen = true
                            logOpen = false
                            scope.launch { drawerState.open() }
                        },
                        botsOpen = botsOpen && drawerState.isOpen,
                        onLogClick = {
                            networkOpen = false
                            botsOpen = false
                            logOpen = true
                            scope.launch { drawerState.open() }
                        },
                        logOpen = logOpen && drawerState.isOpen
                    )
                    content()
                }
            }
        }
    }
}