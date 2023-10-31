package lunalauf.rms.centralapp.components.dialogs.create.challenge

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import lunalauf.rms.centralapp.components.commons.OptionTile
import lunalauf.rms.centralapp.components.commons.tryRequestFocusWithScope
import lunalauf.rms.modelapi.ModelState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChallengeScreen(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    modelState: ModelState.Loaded,
    snackBarHostState: SnackbarHostState
) {
    val screenModel = remember { CreateChallengeScreenModel(modelState, snackBarHostState) }

    val coroutineScope = rememberCoroutineScope()
    val nameFocusRequester = remember { FocusRequester() }
    val descriptionFocusRequester = remember { FocusRequester() }
    val durationFocusRequester = remember { FocusRequester() }
    val expireMessageFocusRequester = remember { FocusRequester() }

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
        ) {
            Text(
                text = "Create challenge",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(20.dp))
            Box(
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .alpha(if (screenModel.processing) 0f else 1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .onPreviewKeyEvent {
                                if (it.key == Key.Enter) {
                                    if (it.type == KeyEventType.KeyUp)
                                        descriptionFocusRequester.requestFocus()
                                    return@onPreviewKeyEvent true
                                }
                                return@onPreviewKeyEvent false
                            }
                            .focusRequester(nameFocusRequester),
                        value = screenModel.name,
                        onValueChange = screenModel::updateName,
                        label = { Text("Name") },
                        isError = !screenModel.nameValid,
                        enabled = !screenModel.processing
                    )
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(descriptionFocusRequester),
                        value = screenModel.description,
                        onValueChange = screenModel::updateDescription,
                        label = { Text("Description") },
                        enabled = !screenModel.processing
                    )
                    OptionTile(
                        text = "Expires",
                        checked = screenModel.expires,
                        onCheckedChange = screenModel::updateExpires
                    )
                    if (screenModel.expires) {
                        OutlinedTextField(
                            modifier = Modifier
                                .onPreviewKeyEvent {
                                    if (it.key == Key.Enter) {
                                        if (it.type == KeyEventType.KeyUp)
                                            expireMessageFocusRequester.requestFocus()
                                        return@onPreviewKeyEvent true
                                    }
                                    return@onPreviewKeyEvent false
                                }
                                .focusRequester(durationFocusRequester),
                            value = screenModel.duration,
                            onValueChange = screenModel::updateDuration,
                            label = { Text("Duration") },
                            suffix = { Text("min") },
                            isError = !screenModel.durationValid,
                            enabled = !screenModel.processing
                        )
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(expireMessageFocusRequester),
                            value = screenModel.expireMessage,
                            onValueChange = screenModel::updateExpireMessage,
                            label = { Text("Expire message") },
                            enabled = !screenModel.processing
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = screenModel.receiveImage,
                                onCheckedChange = screenModel::updateReceiveImage
                            )
                            Text("Receive image")
                        }

                        remember { durationFocusRequester.tryRequestFocusWithScope(coroutineScope) }
                    }
                    FilledTonalButton(
                        modifier = Modifier.align(Alignment.End),
                        onClick = { screenModel.createChallenge(onDismissRequest) },
                        enabled = screenModel.nameValid && screenModel.durationValid && !screenModel.processing
                    ) {
                        Text("Create")
                    }
                }
                if (screenModel.processing)
                    CircularProgressIndicator()
            }
        }

        remember { nameFocusRequester.tryRequestFocusWithScope(coroutineScope) }
    }
}