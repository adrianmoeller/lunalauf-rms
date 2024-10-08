package lunalauf.rms.centralapp.components.commons

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.modelapi.DeleteElementResult
import lunalauf.rms.modelapi.ModelState
import org.eclipse.emf.ecore.EObject

@Composable
fun <T : EObject> DeleteElementDialog(
    modifier: Modifier = Modifier,
    element: T,
    onDeleted: () -> Unit,
    modelState: ModelState.Loaded,
    snackBarHostState: SnackbarHostState,
    triggeringContent: @Composable (() -> Unit) -> Unit
) {
    var isOpen by remember { mutableStateOf(false) }

    triggeringContent { isOpen = true }

    if (isOpen) {
        DeleteElementDialog(
            modifier = modifier,
            element = element,
            onClose = { isOpen = false },
            onDeleted = onDeleted,
            modelState = modelState,
            snackBarHostState = snackBarHostState
        )
    }
}

@Composable
fun <T : EObject> DeleteElementDialog(
    modifier: Modifier = Modifier,
    element: T,
    onClose: () -> Unit,
    onDeleted: () -> Unit,
    modelState: ModelState.Loaded,
    snackBarHostState: SnackbarHostState
) {
    var confirmed by remember { mutableStateOf(false) }
    var deletionFailed: String? by remember { mutableStateOf(null) }
    val screenModel = remember {
        object : AbstractScreenModel(modelState) {
            fun deleteElement() {
                launchInModelScope {
                    val elementString = element.toString()
                    when (val result = modelAPI.deleteElement(element)) {
                        DeleteElementResult.Deleted -> {
                            onClose()
                            onDeleted()
                            snackBarHostState.showSnackbar(
                                message = "Deleted $elementString",
                                withDismissAction = true
                            )
                        }

                        is DeleteElementResult.NotDeleted -> {
                            confirmed = false
                            deletionFailed = result.cause
                        }
                    }
                }
            }
        }
    }

    AlertDialog(
        modifier = modifier.width(IntrinsicSize.Max),
        title = { Text("Delete element") },
        text = {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
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
                Text("You are about to delete this element:")
                OutlinedCard {
                    Text(
                        modifier = Modifier.padding(15.dp),
                        text = element.toString(),
                        fontWeight = FontWeight.Bold
                    )
                }
                val constDeletionFailed = deletionFailed
                if (constDeletionFailed != null) {
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
                            text = "Cannot be deleted: $constDeletionFailed",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = confirmed,
                            onCheckedChange = { confirmed = it },
                        )
                        Text("This cannot be undone. Are you sure?")
                        Spacer(Modifier.width(10.dp))
                    }
                }

            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = screenModel::deleteElement,
                enabled = confirmed,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
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