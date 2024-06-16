package lunalauf.rms.centralapp.components.windows

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import kotlinx.coroutines.launch
import lunalauf.rms.centralapp.components.dialogs.CloseAppDialog
import lunalauf.rms.centralapp.components.dialogs.preferences.PublicViewPrefSheet
import lunalauf.rms.centralapp.components.features.BotSheetScreen
import lunalauf.rms.centralapp.components.features.FeatureRail
import lunalauf.rms.centralapp.components.features.LogSheetScreen
import lunalauf.rms.centralapp.components.features.NetworkSheetScreen
import lunalauf.rms.centralapp.components.main.MainScreenModel
import lunalauf.rms.centralapp.components.main.PublicViewScreenModel
import java.awt.Color

@Composable
fun ApplicationScope.MainWindow(
    modifier: Modifier = Modifier,
    colorScheme: ColorScheme,
    mainScreenModel: MainScreenModel,
    publicViewScreenModel: PublicViewScreenModel,
    publicViewAvailable: Boolean,
    showCloseAppDialog: Boolean,
    content: @Composable () -> Unit
) {
    val icon = painterResource("icons/icon.png")
    var closeAppPromptOpen by remember { mutableStateOf(false) }

    Window(
        onCloseRequest = {
            if (showCloseAppDialog) {
                closeAppPromptOpen = true
            } else {
                mainScreenModel.onCloseApplication()
                exitApplication()
            }
        },
        title = "Luna-Lauf",
        icon = icon
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
            var publicViewPrefOpen by remember { mutableStateOf(false) }

            ModalNavigationDrawer(
                modifier = modifier,
                drawerState = drawerState,
                gesturesEnabled = true,
                drawerContent = {
                    ModalDrawerSheet {
                        if (networkOpen) {
                            NetworkSheetScreen(
                                modifier = Modifier.padding(10.dp),
                                networkManager = mainScreenModel.networkManager
                            )
                        }
                        if (botsOpen) {
                            BotSheetScreen(
                                modifier = Modifier.padding(10.dp),
                                botManager = mainScreenModel.botManager
                            )
                        }
                        if (logOpen) {
                            LogSheetScreen(
                                modifier = Modifier.padding(5.dp)
                            )
                        }
                    }
                }
            ) {
                Row(
                    modifier = Modifier.background(color = MaterialTheme.colorScheme.surface)
                ) {
                    FeatureRail(
                        onPublicViewOpenChange = publicViewScreenModel::updateOpen,
                        publicViewOpen = publicViewScreenModel.open,
                        onPublicViewPrefClick = { publicViewPrefOpen = true },
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

            if (publicViewPrefOpen) {
                PublicViewPrefSheet(
                    screenModel = publicViewScreenModel,
                    onClose = { publicViewPrefOpen = false }
                )
            }

            if (closeAppPromptOpen) {
                CloseAppDialog(
                    onDismissRequest = { closeAppPromptOpen = false },
                    onCloseRequest = {
                        mainScreenModel.onCloseApplication()
                        exitApplication()
                    }
                )
            }
        }
    }
}