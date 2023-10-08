package lunalauf.rms.centralapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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
import lunalauf.rms.centralapp.data.model.DataModel
import lunalauf.rms.centralapp.data.preferences.PreferencesState
import lunalauf.rms.centralapp.ui.common.Table
import lunalauf.rms.centralapp.ui.preferences.PreferencesSheet
import lunalauf.rms.modelapi.LunaLaufAPI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileOpenScreen(
    modifier: Modifier = Modifier,
    dataModelState: DataModel.Loaded,
    preferencesState: PreferencesState,
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
                        text = dataModelState.fileName,
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
    ) { innerPadding ->
        if (settingsOpen) {

            PreferencesSheet(
                onClose = { settingsOpen = false },
                autoSaveActive = preferencesState.autoSaveActive,
                onAutoSaveActiveChange = { TODO() },
                autoSaveInterval = preferencesState.autoSaveInterval,
                onAutoSaveIntervalChange = { TODO() },
                onAutoSaveIntervalChangeFinished = { TODO() },
                roundThreshold = preferencesState.roundThreshold,
                onRoundThresholdChange = { TODO() },
                onRoundThresholdChangeFinished = { TODO() },
                saveConnectionsActive = preferencesState.saveConnectionsActive,
                onSaveConnectionsActiveChange = { TODO() }
            )
        }

        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            val runnersState by dataModelState.runners.collectAsState()
            val data = runnersState.runners.map {
                listOf(
                    it.id.toString(),
                    it.name ?: "",
                    it.numOfRounds().toString(),
                    it.team?.name ?: "",
                    it.totalAmount().toString()
                )
            }
            Table(
                modifier = Modifier.weight(1f),
                header = listOf("ID", "Name", "Rounds", "Team", "Total Amount"),
                data = data,
                weights = listOf(1f, 3f, 1f, 3f, 1.5f)
            )

            Button(
                onClick = {
                    LunaLaufAPI.addNewRunner(1234, "")
                }
            ) {
                Text("Add Runner 1234")
            }
        }
    }
}