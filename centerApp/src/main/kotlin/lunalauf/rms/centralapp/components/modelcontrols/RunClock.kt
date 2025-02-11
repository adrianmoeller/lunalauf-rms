package lunalauf.rms.centralapp.components.modelcontrols

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Pause
import compose.icons.fontawesomeicons.solid.Play
import lunalauf.rms.centralapp.components.commons.IconSize
import lunalauf.rms.model.common.RunTimerState

@Composable
fun RunClock(
    modifier: Modifier = Modifier,
    remainingTime: String,
    state: RunTimerState,
    onStartStopClick: () -> Unit,
    onResetClick: () -> Unit,
    runDuration: Int,
    onRunDurationChange: (Int) -> Unit
) {
    var editRunDurationOpen by remember { mutableStateOf(false) }
    var newRunDuration by remember { mutableStateOf(runDuration.toString()) }
    val parsedNewRunDuration = newRunDuration.trim().toIntOrNull()?.takeIf { it >= 0 }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = remainingTime,
            style = MaterialTheme.typography.displayMedium
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            IconButton(
                onClick = {
                    newRunDuration = runDuration.toString()
                    editRunDurationOpen = true
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Edit run duration"
                )
            }
            FilledIconButton(
                modifier = Modifier.size(50.dp),
                onClick = onStartStopClick
            ) {
                Icon(
                    modifier = Modifier.size(IconSize.small),
                    imageVector = if (state == RunTimerState.RUNNING) FontAwesomeIcons.Solid.Pause
                    else FontAwesomeIcons.Solid.Play,
                    contentDescription = when (state) {
                        RunTimerState.RUNNING -> "Pause run"
                        RunTimerState.PAUSED -> "Resume run"
                        RunTimerState.EXPIRED -> "Start run"
                    }
                )
            }
            IconButton(
                onClick = onResetClick,
                enabled = state == RunTimerState.PAUSED
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Reset run"
                )
            }
        }
    }

    if (editRunDurationOpen) {
        AlertDialog(
            title = {
                Text("Update run duration")
            },
            text = {
                OutlinedTextField(
                    label = { Text("Run duration") },
                    value = newRunDuration,
                    onValueChange = { newRunDuration = it },
                    suffix = {
                        Text("min")
                    }
                )
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        onRunDurationChange(parsedNewRunDuration ?: 0)
                        editRunDurationOpen = false
                    },
                    enabled = parsedNewRunDuration != null
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { editRunDurationOpen = false }
                ) {
                    Text("Cancel")
                }
            },
            onDismissRequest = { editRunDurationOpen = false }
        )
    }
}