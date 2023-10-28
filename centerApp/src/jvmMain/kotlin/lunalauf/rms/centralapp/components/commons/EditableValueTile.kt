package lunalauf.rms.centralapp.components.commons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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

@Composable
fun <V> EditableValueTile(
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
    var newValue by remember { mutableStateOf(TextFieldValue(value.toString())) }

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
            val displayValue = if (value == null) "None"
            else if (unit == null) value.toString()
            else "$value $unit"
            Text(
                text = displayValue,
                fontWeight = FontWeight.Bold
            )
            FilledTonalIconButton(
                onClick = {
                    val text = value.toString()
                    newValue = TextFieldValue(
                        text = text,
                        selection = TextRange(0, text.length)
                    )
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
fun <V> EditDialog(
    editTitle: String,
    editDialogOpen: Boolean,
    onClose: () -> Unit,
    onValueChange: (V) -> Unit,
    valueName: String,
    newValue: TextFieldValue,
    onNewValueChange: (TextFieldValue) -> Unit,
    parser: (String) -> V?,
    default: V,
    unit: String?
) {
    if (editDialogOpen) {
        val parsedNewValue = parser(newValue.text)
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
