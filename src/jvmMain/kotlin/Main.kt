import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.application
import ui.screens.FileOpenScreen
import ui.screens.MainWindow
import ui.screens.NoFileOpenScreen
import ui.screens.PublicViewScreen

@Composable
@Preview
fun ApplicationScope.App() {
//    val colorScheme = darkColorScheme()
    val colorScheme = lightColorScheme()

    var fileOpen by remember { mutableStateOf(true) } // TMP

    var publicViewOpen by remember { mutableStateOf(false) }

    MainWindow(
        colorScheme = colorScheme,
        onPublicViewOpenChange = { publicViewOpen = it },
        publicViewOpen = publicViewOpen,
        onPublicViewSettingsClick = { /* TODO */ },
        publicViewAvailable = fileOpen
    ) {
        if (fileOpen) {
            FileOpenScreen(
                fileName = "file.ll",
                onMenuClick = {}
            )
        } else {
            NoFileOpenScreen(
                onNewClick = {},
                onOpenClick = {}
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
