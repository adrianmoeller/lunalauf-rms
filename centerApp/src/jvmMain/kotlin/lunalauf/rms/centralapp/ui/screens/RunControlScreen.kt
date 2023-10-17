package lunalauf.rms.centralapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import lunalauf.rms.centralapp.ui.components.RunClock
import lunalauf.rms.centralapp.ui.screenmodels.RunControlScreenModel
import lunalauf.rms.modelapi.ModelState

@Composable
fun RunControlScreen(
    modifier: Modifier = Modifier,
    modelState: ModelState.Loaded
) {
    val screenModel = remember { RunControlScreenModel(modelState) }
    val commons by modelState.common.collectAsState()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        RunClock(
            modifier = Modifier.padding(
                top = 20.dp,
                bottom = 10.dp
            ),
            remainingTime = "00:29:34",
            running = true,
            onStartStopClick = {},
            onResetClick = {},
            runDuration = 90.toUInt(),
            onRunDurationChange = {}
        )
        Row(
            modifier = Modifier
                .padding(20.dp)
                .sizeIn(
                    maxWidth = 800.dp,
                    maxHeight = 350.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            RunControlCard(
                modifier = Modifier.weight(1f),
                title = "Sponsoring pool"
            ) {
                EditableValueTile(
                    name = "Amount",
                    value = commons.sponsorPoolAmount,
                    onValueChange = screenModel::updateSponsoringPoolAmount,
                    parser = { it.trim().toDoubleOrNull() },
                    default = 0.0,
                    unit = "€",
                    editTitle = "Update pool amount"
                )
                EditableValueTile(
                    name = "Rounds to be reached",
                    value = commons.sponsorPoolRounds,
                    onValueChange = screenModel::updateSponsoringPoolRounds,
                    parser = { it.trim().toUIntOrNull()?.toInt() },
                    default = 0,
                    editTitle = "Update pool rounds"
                )
            }
            RunControlCard(
                modifier = Modifier.weight(1f),
                title = "Additional donations"
            ) {
                EditableValueTile(
                    name = "Amount",
                    value = commons.additionalContribution,
                    onValueChange = screenModel::updateAdditionalContribution,
                    parser = { it.trim().toDoubleOrNull() },
                    default = 0.0,
                    unit = "€",
                    editTitle = "Update additional donations"
                )
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    var addToAddContrDialogOpen by remember { mutableStateOf(false) }
                    var addToAddContrValue by remember { mutableStateOf(commons.additionalContribution.toString()) }
                    OutlinedButton(
                        modifier = Modifier.padding(bottom = 5.dp),
                        onClick = {
                            addToAddContrValue = ""
                            addToAddContrDialogOpen = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(2.dp))
                        Text("Add to amount")
                    }
                    EditDialog(
                        editTitle = "Add to amount",
                        editDialogOpen = addToAddContrDialogOpen,
                        onClose = { addToAddContrDialogOpen = false },
                        onValueChange = screenModel::addToAdditionalContribution,
                        valueName = "Amount to add",
                        newValue = addToAddContrValue,
                        onNewValueChange = { addToAddContrValue = it },
                        parser = { it.trim().toDoubleOrNull() },
                        default = 0.0,
                        unit = "€"
                    )
                }

            }
        }
    }
}

@Composable
private fun RunControlCard(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 15.dp,
                vertical = 10.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp),
                content = content
            )
        }
    }
}

@Composable
private fun <V> EditableValueTile(
    modifier: Modifier = Modifier,
    name: String,
    value: V,
    onValueChange: (V) -> Unit,
    parser: (String) -> V?,
    default: V,
    unit: String? = null,
    editTitle: String,
) {
    var editDialogOpen by remember { mutableStateOf(false) }
    var newValue by remember { mutableStateOf(value.toString()) }

    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("$name:")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = if (unit == null) value.toString() else "$value $unit",
                fontWeight = FontWeight.Bold
            )
            FilledTonalIconButton(
                onClick = {
                    newValue = value.toString()
                    editDialogOpen = true
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Edit"
                )
            }
        }
    }
    EditDialog(
        editTitle = editTitle,
        editDialogOpen = editDialogOpen,
        onClose = { editDialogOpen = false },
        onValueChange = onValueChange,
        valueName = name,
        newValue = newValue,
        onNewValueChange = { newValue = it },
        parser = parser,
        default = default,
        unit = unit
    )
}

@Composable
private fun <V> EditDialog(
    editTitle: String,
    editDialogOpen: Boolean,
    onClose: () -> Unit,
    onValueChange: (V) -> Unit,
    valueName: String,
    newValue: String,
    onNewValueChange: (String) -> Unit,
    parser: (String) -> V?,
    default: V,
    unit: String?
) {
    if (editDialogOpen) {
        val parsedNewValue = parser(newValue)
        val focusRequester = remember { FocusRequester() }

        AlertDialog(
            title = { Text(editTitle) },
            text = {
                OutlinedTextField(
                    modifier = Modifier
                        .onPreviewKeyEvent {
                            if (it.key == Key.Enter) {
                                if (it.type == KeyEventType.KeyUp && parsedNewValue != null) {
                                    onValueChange(parsedNewValue)
                                    onClose()
                                }
                                return@onPreviewKeyEvent true
                            }
                            return@onPreviewKeyEvent false
                        }
                        .focusRequester(focusRequester),
                    label = { Text(valueName) },
                    value = newValue,
                    onValueChange = onNewValueChange,
                    suffix = unit?.let { { Text(it) } },
                    isError = parsedNewValue == null
                )

            },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        onValueChange(parsedNewValue ?: default)
                        onClose()
                    },
                    enabled = parsedNewValue != null
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onClose
                ) {
                    Text("Cancel")
                }
            },
            onDismissRequest = onClose
        )

        focusRequester.requestFocus()
    }
}
