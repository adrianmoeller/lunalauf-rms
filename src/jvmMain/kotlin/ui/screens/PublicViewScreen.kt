package ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.rememberWindowState
import ui.common.PublicViewTable
import ui.publicViewTypography

@Composable
fun ApplicationScope.PublicViewScreen(
    open: Boolean,
    onClose: () -> Unit,
    borderColor: Color
) {
    if (open) {
        val borderWidth = 5.dp
        val windowState = rememberWindowState()

        Window(
            onCloseRequest = onClose,
            title = "Luna-Lauf - Public View",
            state = windowState,
            onKeyEvent = {
                if (it.key == Key.F && it.type == KeyEventType.KeyDown) {
                    if (windowState.placement != WindowPlacement.Fullscreen)
                        windowState.placement = WindowPlacement.Fullscreen
                    else
                        windowState.placement = WindowPlacement.Floating
                    return@Window true
                }
                return@Window false
            }
        ) {
            MaterialTheme(
                colorScheme = lightColorScheme(),
                typography = publicViewTypography
            ) {
                Surface(
                    color = borderColor
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(borderWidth)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(borderWidth)
                        ) {
                            TeamPanel(
                                modifier = Modifier
                                    .fillMaxSize()
                            )
                            RunnerPanel(
                                modifier = Modifier
                                    .fillMaxSize()
                            )
                        }
                        CommonPanel(
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

private object TeamPanelTextStyles {
    val header = TextStyle(fontWeight = FontWeight.Light)
    val headerSum = TextStyle(fontWeight = FontWeight.Normal)
}

@Composable
private fun TeamPanel(
    modifier: Modifier = Modifier
) {
    PublicViewTable(
        modifier = modifier,
        header = listOf("Team", "Rounds"),
        data = listOf(
            listOf("A", "4"),
            listOf("B", "10"),
            listOf("D", "20"),
            listOf("E", "30")
        ),
        weights = listOf(3f, 2f),
        headerTextStyles = buildMap {
            put(0, TeamPanelTextStyles.header)
            put(1, TeamPanelTextStyles.headerSum)
        },
        dataTextStyles = emptyMap(),
        showPlacements = true
    )
    MaterialTheme.typography
}

@Composable
private fun RunnerPanel(
    modifier: Modifier = Modifier
) {

}

@Composable
private fun CommonPanel(
    modifier: Modifier = Modifier
) {

}
