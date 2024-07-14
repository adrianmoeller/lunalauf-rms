package lunalauf.rms.centralapp

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.application
import lunalauf.rms.centralapp.components.main.FileOpenScreen
import lunalauf.rms.centralapp.components.main.MainScreenModel
import lunalauf.rms.centralapp.components.main.NoFileOpenScreen
import lunalauf.rms.centralapp.components.main.PublicViewScreenModel
import lunalauf.rms.centralapp.components.windows.MainWindow
import lunalauf.rms.centralapp.components.windows.PublicViewWindow
import lunalauf.rms.modelapi.ModelState
import lunalauf.rms.utilities.logging.Logger
import lunalauf.rms.utilities.logging.configureStartUpErrorLogging

@Composable
@Preview
fun ApplicationScope.App() {
    val mainScreenModel = remember { MainScreenModel() }
    val publicViewScreenModel = remember { PublicViewScreenModel(mainScreenModel.snackBarHostState) }
    val modelState by mainScreenModel.modelState.collectAsState()
    var useDarkTheme by remember { mutableStateOf(true) }

    val constModelState = modelState
    MainWindow(
        useDarkTheme = useDarkTheme,
        mainScreenModel = mainScreenModel,
        publicViewScreenModel = publicViewScreenModel,
        publicViewAvailable = constModelState is ModelState.Loaded,
        showCloseAppDialog = constModelState is ModelState.Loaded
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
                modelResourceManager = mainScreenModel.modelResourceManager,
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
            mainScreenModel = mainScreenModel,
            publicViewScreenModel = publicViewScreenModel,
            modelState = constModelState
        )
    }
}

fun main() {
    configureStartUpErrorLogging()
    Logger.configure()

    application {
        App()
    }
}
