package lunalauf.rms.centralapp

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.application
import lunalauf.rms.centralapp.data.model.DataModel
import lunalauf.rms.centralapp.ui.screenmodels.MainScreenModel
import lunalauf.rms.centralapp.ui.screens.FileOpenScreen
import lunalauf.rms.centralapp.ui.screens.MainWindow
import lunalauf.rms.centralapp.ui.screens.NoFileOpenScreen
import lunalauf.rms.centralapp.ui.screens.PublicViewScreen

@Composable
@Preview
fun ApplicationScope.App() {
//    val colorScheme = darkColorScheme()
    val colorScheme = lightColorScheme()

    val mainScreenModel = remember { MainScreenModel() }
    var publicViewOpen by remember { mutableStateOf(false) }
    val modelState = mainScreenModel.dataModelState

    MainWindow(
        colorScheme = colorScheme,
        onPublicViewOpenChange = { publicViewOpen = it },
        publicViewOpen = publicViewOpen,
        onPublicViewSettingsClick = { /* TODO */ },
        publicViewAvailable = modelState is DataModel.Loaded
    ) {
        if (modelState is DataModel.Loaded) {
            FileOpenScreen(
                dataModelState = modelState,
                preferencesState = mainScreenModel.preferencesState,
                onMenuClick = { /* TODO */ }
            )
        } else {
            NoFileOpenScreen(
                onNewClick = mainScreenModel::newFile,
                onOpenClick = mainScreenModel::openFile
            )
        }
    }
    PublicViewScreen(
        open = publicViewOpen,
        onClose = { publicViewOpen = false },
        borderColor = Color.Gray
    )
}

fun main() = application {
    App()
}
