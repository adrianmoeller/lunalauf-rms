package lunalauf.rms.centralapp.components.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import lunalauf.rms.centralapp.components.commons.IconSize

@Composable
fun CloseAppDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onCloseRequest: () -> Unit
) {
    var confirmedDiscard by remember { mutableStateOf(false) }

    AlertDialog(
        modifier = modifier.width(IntrinsicSize.Max),
        title = { Text("Close application") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    modifier = Modifier.size(IconSize.extraLarge),
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text("You are about to close the application. The model might be in unsaved state. Are you sure?")
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = confirmedDiscard,
                        onCheckedChange = { confirmedDiscard = it },
                    )
                    Text("Check to enable close option")
                    Spacer(Modifier.width(10.dp))
                }
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = onCloseRequest,
                enabled = confirmedDiscard,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error
                )

            ) {
                Text("Close")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
            ) {
                Text("Cancel")
            }
        },
        onDismissRequest = onDismissRequest
    )
}