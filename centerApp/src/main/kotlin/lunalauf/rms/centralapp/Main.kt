package lunalauf.rms.centralapp

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ExclamationCircle
import lunalauf.rms.centralapp.components.commons.IconSize
import lunalauf.rms.centralapp.components.main.FileOpenScreen
import lunalauf.rms.centralapp.components.main.MainScreenModel
import lunalauf.rms.centralapp.components.main.NoFileOpenScreen
import lunalauf.rms.centralapp.components.main.PublicViewScreenModel
import lunalauf.rms.centralapp.components.windows.MainWindow
import lunalauf.rms.centralapp.components.windows.PublicViewWindow
import lunalauf.rms.centralapp.theme.AppTheme
import lunalauf.rms.model.api.ModelManager
import lunalauf.rms.model.api.ModelState
import lunalauf.rms.utilities.logging.Logger
import lunalauf.rms.utilities.logging.configureStartUpErrorLogging

@Composable
@Preview
fun ApplicationScope.App() {
    val mainScreenModel = remember { MainScreenModel() }
    val publicViewScreenModel = remember { PublicViewScreenModel(mainScreenModel.snackBarHostState) }
    val modelManager = mainScreenModel.modelManager
    var useDarkTheme by remember { mutableStateOf(true) }

    if (modelManager is ModelManager.Available) {
        val modelState by modelManager.model.collectAsState()
        val constModelState = modelState

        MainWindow(
            useDarkTheme = useDarkTheme,
            mainScreenModel = mainScreenModel,
            publicViewScreenModel = publicViewScreenModel,
            publicViewAvailable = modelState is ModelState.Loaded,
            showCloseAppDialog = modelState is ModelState.Loaded
        ) {
            when (constModelState) {
                is ModelState.Unloaded -> NoFileOpenScreen(
                    onNewClick = mainScreenModel::newFile,
                    onOpenClick = mainScreenModel::openFile,
                    snackBarHostState = mainScreenModel.snackBarHostState
                )

                is ModelState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                is ModelState.Loaded -> FileOpenScreen(
                    modelManager = modelManager,
                    botManager = mainScreenModel.botManager,
                    modelState = constModelState,
                    onMenuNewFile = mainScreenModel::newFile,
                    onMenuOpenFile = mainScreenModel::openFile,
                    onMenuSaveFile = mainScreenModel::saveFile,
                    onMenuCloseFile = mainScreenModel::closeFile,
                    onSwitchTheme = { useDarkTheme = !useDarkTheme },
                    useDarkTheme = useDarkTheme,
                    snackBarHostState = mainScreenModel.snackBarHostState
                )
            }
        }
        if (constModelState is ModelState.Loaded) {
            PublicViewWindow(
                publicViewScreenModel = publicViewScreenModel,
                event = constModelState.event
            )
        }
    } else {
        Window(
            onCloseRequest = { exitApplication() },
            title = "Luna-Lauf"
        ) {
            AppTheme(
                darkTheme = useDarkTheme
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(IconSize.extraLarge),
                        imageVector = FontAwesomeIcons.Solid.ExclamationCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Failed to initialize model manager"
                    )
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
