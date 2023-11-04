package lunalauf.rms.centralapp

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.application
import lunalauf.rms.centralapp.components.main.MainScreenModel
import lunalauf.rms.centralapp.components.main.FileOpenScreen
import lunalauf.rms.centralapp.components.windows.MainWindow
import lunalauf.rms.centralapp.components.main.NoFileOpenScreen
import lunalauf.rms.centralapp.components.main.PublicViewScreenModel
import lunalauf.rms.centralapp.components.windows.PublicViewWindow
import lunalauf.rms.modelapi.ModelState
import org.apache.log4j.BasicConfigurator

@Composable
@Preview
fun ApplicationScope.App() {
    val colorScheme = darkColorScheme()
//    val colorScheme = lightColorScheme()

    val mainScreenModel = remember { MainScreenModel() }
    val publicViewScreenModel = remember { PublicViewScreenModel(mainScreenModel.snackBarHostState) }
    val modelState = mainScreenModel.modelState

    MainWindow(
        colorScheme = colorScheme,
        publicViewScreenModel = publicViewScreenModel,
        publicViewAvailable = modelState is ModelState.Loaded
    ) {
        when (modelState) {
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
                modelState = modelState,
                onMenuNewFile = mainScreenModel::newFile,
                onMenuOpenFile = mainScreenModel::openFile,
                onMenuSaveFile = mainScreenModel::saveFile,
                onMenuCloseFile = mainScreenModel::closeFile,
                snackBarHostState = mainScreenModel.snackBarHostState
            )
        }
    }
    if (modelState is ModelState.Loaded) {
        PublicViewWindow(
            mainScreenModel = mainScreenModel,
            publicViewScreenModel = publicViewScreenModel,
            modelState = modelState,
            borderColor = Color.Gray
        )
    }
}

fun main() {
    BasicConfigurator.configure()

    application {
        App()
    }
}
