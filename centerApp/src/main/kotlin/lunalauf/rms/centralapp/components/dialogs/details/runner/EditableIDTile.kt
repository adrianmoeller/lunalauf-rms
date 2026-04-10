package lunalauf.rms.centralapp.components.dialogs.details.runner

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import lunalauf.rms.centralapp.components.dialogs.ScanChipField
import lunalauf.rms.centralapp.components.dialogs.ScanChipScreenModel
import lunalauf.rms.model.api.ModelState

@Composable
fun EditableIDTile(
    modifier: Modifier = Modifier,
    value: Long,
    onIdChange: (Long) -> Unit,
    modelState: ModelState.Loaded
) {
    var editDialogOpen by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("ID:")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = value.toString(),
                fontWeight = FontWeight.Bold
            )
            FilledTonalIconButton(
                onClick = { editDialogOpen = true }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Edit"
                )
            }
        }
    }
    EditIdDialog(
        editDialogOpen = editDialogOpen,
        onClose = { editDialogOpen = false },
        onIdChange = onIdChange,
        modelState = modelState
    )
}

@Composable
fun EditIdDialog(
    editDialogOpen: Boolean,
    onClose: () -> Unit,
    onIdChange: (Long) -> Unit,
    modelState: ModelState.Loaded
) {
    if (editDialogOpen) {
        val screenModel = remember { ScanChipScreenModel(modelState) }
        var showError by remember { mutableStateOf(false) }

        AlertDialog(
            modifier = Modifier.width(IntrinsicSize.Max),
            title = { Text("Update ID") },
            text = {
                Column(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    if (showError) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Text(
                                modifier = Modifier.padding(
                                    vertical = 10.dp,
                                    horizontal = 15.dp
                                ),
                                text = "This ID already exists",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    ScanChipField(
                        showError = screenModel.showError,
                        onNumberKeyEvent = screenModel::toIdBuffer,
                        onEnterKeyEvent = {
                            screenModel.processBufferedId(
                                onKnown = { showError = true },
                                onUnknown = {
                                    onIdChange(it)
                                    onClose()
                                }
                            )
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = onClose
                ) {
                    Text("Cancel")
                }
            },
            onDismissRequest = onClose
        )
    }
}