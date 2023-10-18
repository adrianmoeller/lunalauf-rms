package lunalauf.rms.centralapp

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.application
import lunalauf.rms.centralapp.ui.components.main.MainScreenModel
import lunalauf.rms.centralapp.ui.components.main.FileOpenScreen
import lunalauf.rms.centralapp.ui.components.windows.MainWindow
import lunalauf.rms.centralapp.ui.components.main.NoFileOpenScreen
import lunalauf.rms.centralapp.ui.components.windows.PublicViewWindow
import lunalauf.rms.modelapi.ModelState
import org.apache.log4j.BasicConfigurator

@Composable
@Preview
fun ApplicationScope.App() {
//    val colorScheme = darkColorScheme()
    val colorScheme = lightColorScheme()

    val mainScreenModel = remember { MainScreenModel() }
    var publicViewOpen by remember { mutableStateOf(false) }
    val modelState = mainScreenModel.modelState

    MainWindow(
        colorScheme = colorScheme,
        onPublicViewOpenChange = { publicViewOpen = it },
        publicViewOpen = publicViewOpen,
        onPublicViewSettingsClick = { /* TODO */ },
        publicViewAvailable = modelState is ModelState.Loaded
    ) {
        when (modelState) {
            is ModelState.Unloaded -> NoFileOpenScreen(
                onNewClick = mainScreenModel::newFile,
                onOpenClick = mainScreenModel::openFile
            )
            is ModelState.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            is ModelState.Loaded -> FileOpenScreen(
                modelResourceManager = mainScreenModel.modelResourceManager,
                modelState = modelState,
                onMenuNewFile = mainScreenModel::newFile,
                onMenuOpenFile = mainScreenModel::openFile,
                onMenuSaveFile = mainScreenModel::saveFile,
                onMenuCloseFile = mainScreenModel::closeFile
            )
        }
    }
    PublicViewWindow(
        open = publicViewOpen,
        onClose = { publicViewOpen = false },
        borderColor = Color.Gray
    )
}

fun main() {
    BasicConfigurator.configure()

    application {
        App()
    }
}
