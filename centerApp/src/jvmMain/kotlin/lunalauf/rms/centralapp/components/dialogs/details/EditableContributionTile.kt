package lunalauf.rms.centralapp.components.dialogs.details

import LunaLaufLanguage.ContrType
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import lunalauf.rms.centralapp.components.commons.OptionTile
import lunalauf.rms.centralapp.components.commons.tryRequestFocusWithScope

@Composable
fun EditableContributionTile(
    modifier: Modifier = Modifier,
    type: ContrType,
    amountFixed: Double,
    amountPerRound: Double,
    onValuesChange: (ContrType, Double, Double) -> Unit
) {
    var editDialogOpen by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Contribution:")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            when (type) {
                ContrType.PERROUND -> {
                    Card(
                        modifier = Modifier.width(IntrinsicSize.Max)
                    ) {
                        ContributionRow(
                            title = "Per round",
                            value = amountPerRound
                        )
                    }
                }

                ContrType.FIXED -> {
                    Card(
                        modifier = Modifier.width(IntrinsicSize.Max)
                    ) {
                        ContributionRow(
                            title = "Fixed",
                            value = amountFixed
                        )
                    }
                }

                ContrType.BOTH -> {
                    Card(
                        modifier = Modifier.width(IntrinsicSize.Max)
                    ) {
                        ContributionRow(
                            title = "Fixed",
                            value = amountFixed
                        )
                        Divider()
                        ContributionRow(
                            title = "Per round",
                            value = amountPerRound
                        )
                    }
                }

                ContrType.NONE -> {
                    Text(
                        text = "None",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
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
    EditContributionDialog(
        editDialogOpen = editDialogOpen,
        type = type,
        amountFixed = amountFixed,
        amountPerRound = amountPerRound,
        onClose = { editDialogOpen = false },
        onValuesChange = onValuesChange
    )
}

@Composable
private fun ContributionRow(
    title: String,
    value: Double
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$title:",
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(Modifier.width(5.dp))
        Text(
            modifier = Modifier.padding(bottom = 3.dp),
            text = "$value €",
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EditContributionDialog(
    editDialogOpen: Boolean,
    type: ContrType,
    amountFixed: Double,
    amountPerRound: Double,
    onClose: () -> Unit,
    onValuesChange: (ContrType, Double, Double) -> Unit
) {
    if (editDialogOpen) {
        var fixedChecked by remember { mutableStateOf(type == ContrType.FIXED || type == ContrType.BOTH) }
        var perRoundChecked by remember { mutableStateOf(type == ContrType.PERROUND || type == ContrType.BOTH) }

        var newAmountFixed by remember { mutableStateOf(TextFieldValue(amountFixed.toString())) }
        var newAmountPerRound by remember {
            mutableStateOf(
                TextFieldValue(
                    text = amountPerRound.toString(),
                    selection = TextRange(0, amountPerRound.toString().length)
                )
            )
        }

        val parsedNewAmountFixed = newAmountFixed.text.trim().toDoubleOrNull()
        val parsedNewAmountPerRound = newAmountPerRound.text.trim().toDoubleOrNull()
        val allValid = parsedNewAmountFixed != null && parsedNewAmountPerRound != null

        val onUpdate = {
            val contributionType = when {
                fixedChecked && perRoundChecked -> ContrType.BOTH
                fixedChecked -> ContrType.FIXED
                perRoundChecked -> ContrType.PERROUND
                else -> ContrType.NONE
            }
            onValuesChange(contributionType, parsedNewAmountFixed ?: 0.0, parsedNewAmountPerRound ?: 0.0)
            onClose()
        }

        AlertDialog(
            title = { Text("Update Team") },
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    ContributionTypeCard(
                        modifier = Modifier.weight(1f),
                        text = "Fixed",
                        textFieldLabel = "Fixed amount",
                        checked = fixedChecked,
                        onCheckedChange = {
                            fixedChecked = it
                            if (it) {
                                val text = amountFixed.toString()
                                newAmountFixed = TextFieldValue(
                                    text = text,
                                    selection = TextRange(0, text.length)
                                )
                            }
                        },
                        newAmount = newAmountFixed,
                        onNewAmountChange = { newAmountFixed = it },
                        isError = parsedNewAmountFixed == null,
                        onUpdateRequest = { if (allValid) onUpdate() }
                    )
                    ContributionTypeCard(
                        modifier = Modifier.weight(1f),
                        text = "Per round",
                        textFieldLabel = "Amount per round",
                        checked = perRoundChecked,
                        onCheckedChange = {
                            perRoundChecked = it
                            if (it) {
                                val text = amountPerRound.toString()
                                newAmountPerRound = TextFieldValue(
                                    text = text,
                                    selection = TextRange(0, text.length)
                                )
                            }
                        },
                        newAmount = newAmountPerRound,
                        onNewAmountChange = { newAmountPerRound = it },
                        isError = parsedNewAmountPerRound == null,
                        onUpdateRequest = { if (allValid) onUpdate() }
                    )
                }
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = onUpdate,
                    enabled = allValid
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
    }
}

@Composable
private fun ContributionTypeCard(
    modifier: Modifier = Modifier,
    text: String,
    textFieldLabel: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    newAmount: TextFieldValue,
    onNewAmountChange: (TextFieldValue) -> Unit,
    isError: Boolean,
    onUpdateRequest: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    OutlinedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 15.dp,
                vertical = 10.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OptionTile(
                text = text,
                checked = checked,
                onCheckedChange = onCheckedChange
            )
            if (checked) {
                OutlinedTextField(
                    modifier = Modifier
                        .padding(bottom = 5.dp)
                        .onPreviewKeyEvent {
                            if (it.key == Key.Enter) {
                                if (it.type == KeyEventType.KeyUp)
                                    onUpdateRequest()
                                return@onPreviewKeyEvent true
                            }
                            return@onPreviewKeyEvent false
                        }
                        .focusRequester(focusRequester),
                    label = { Text(textFieldLabel) },
                    value = newAmount,
                    onValueChange = onNewAmountChange,
                    suffix = { Text("€") },
                    isError = isError
                )

                focusRequester.tryRequestFocusWithScope(coroutineScope)
            }
        }
    }
}
