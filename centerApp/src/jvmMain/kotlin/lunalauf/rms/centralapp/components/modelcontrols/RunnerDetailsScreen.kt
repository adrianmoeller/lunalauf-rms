package lunalauf.rms.centralapp.components.modelcontrols

import LunaLaufLanguage.Runner
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import lunalauf.rms.centralapp.components.commons.EditableValueTile
import lunalauf.rms.centralapp.components.commons.FullScreenDialog
import lunalauf.rms.centralapp.components.dialogs.ScanChipField
import lunalauf.rms.centralapp.components.dialogs.ScanChipScreenModel
import lunalauf.rms.modelapi.ModelState

@Composable
fun RunnerDetailsScreen(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    modelState: ModelState.Loaded,
    runner: Runner
) {
    val screenModel = remember { RunnerDetailsScreenModel(modelState) }

    FullScreenDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = "Runner: ${if (runner.name.isNullOrBlank()) runner.id else runner.name}",
        maxWidth = 800.dp
    ) {
        OutlinedCard(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 15.dp,
                    vertical = 10.dp
                ),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                EditableIDTile(
                    value = runner.id.toULong(),
                    onIdChange = {
                        screenModel.updateID(runner, it)
                    },
                    modelState = modelState
                )
                EditableValueTile(
                    name = "Name",
                    value = runner.name,
                    onValueChange = {screenModel.updateName(runner, it)},
                    parser = screenModel::validateName,
                    default = "",
                    editTitle = "Update name"
                )

            }
        }
    }
}

@Composable
private fun EditableIDTile(
    modifier: Modifier = Modifier,
    value: ULong,
    onIdChange: (ULong) -> Unit,
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
private fun EditIdDialog(
    editDialogOpen: Boolean,
    onClose: () -> Unit,
    onIdChange: (ULong) -> Unit,
    modelState: ModelState.Loaded
) {
    if (editDialogOpen) {
        val screenModel = remember { ScanChipScreenModel(modelState) }

        AlertDialog(
            modifier = Modifier.width(IntrinsicSize.Max),
            title = { Text("Update ID") },
            text = {
                Column(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ScanChipField(
                        showError = screenModel.showError,
                        onNumberKeyEvent = screenModel::toIdBuffer,
                        onEnterKeyEvent = {
                            screenModel.processBufferedId(
                                onKnown = {
                                    // TODO show card
                                },
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
