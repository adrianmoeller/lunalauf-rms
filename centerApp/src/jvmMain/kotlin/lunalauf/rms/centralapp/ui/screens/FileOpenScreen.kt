package lunalauf.rms.centralapp.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import lunalauf.rms.centralapp.ui.preferences.PreferencesSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileOpenScreen(
    modifier: Modifier = Modifier,
    fileName: String,
    onMenuClick: () -> Unit
) {
    var settingsOpen by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier
                    .shadow(5.dp),
                title = {
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onMenuClick
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = "Menu"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { settingsOpen = true }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) {
        if (settingsOpen) {
            var autoSaveActive by remember { mutableStateOf(false) }
            var autoSaveInterval by remember { mutableStateOf(30f) }

            PreferencesSheet(
                onClose = { settingsOpen = false },
                autoSaveActive = autoSaveActive,
                onAutoSaveActiveChange = { autoSaveActive = !autoSaveActive },
                autoSaveInterval = autoSaveInterval,
                onAutoSaveIntervalChange = { autoSaveInterval = it },
                onAutoSaveIntervalChangeFinished = {},
                roundThreshold = 40f,
                onRoundThresholdChange = {},
                onRoundThresholdChangeFinished = {},
                saveConnectionsActive = false,
                onSaveConnectionsActiveChange = {}
            )
        }
    }
}