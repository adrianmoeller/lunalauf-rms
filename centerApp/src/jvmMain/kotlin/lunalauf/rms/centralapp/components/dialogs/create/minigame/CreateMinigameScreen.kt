package lunalauf.rms.centralapp.components.dialogs.create.minigame

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
import lunalauf.rms.centralapp.components.commons.tryRequestFocusWithScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMinigameScreen(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
) {
    val screenModel = remember {  }

    val coroutineScope = rememberCoroutineScope()
    val idFocusRequester = remember { FocusRequester() }
    val nameFocusRequester = remember { FocusRequester() }

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
                text = "Scan to show",
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .onPreviewKeyEvent {
                                if (it.key == Key.Enter) {
                                    if (it.type == KeyEventType.KeyUp && screenModel.idValid)
                                        nameFocusRequester.requestFocus()
                                    return@onPreviewKeyEvent true
                                }
                                return@onPreviewKeyEvent false
                            }
                            .focusRequester(idFocusRequester),
                        value = screenModel.id,
                        onValueChange = screenModel::updateid,
                        label = { Text("ID") },
                        isError = !screenModel.idValid,
                        enabled = !screenModel.processing
                    )
                    OutlinedTextField(
                        modifier = Modifier
                            .onPreviewKeyEvent {
                                if (it.key == Key.Enter) {
                                    if (it.type == KeyEventType.KeyUp && screenModel.nameValid)
                                        screenModel.createMinigame()
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
                }
                if (screenModel.processing)
                    CircularProgressIndicator()
            }
        }
    }

    idFocusRequester.tryRequestFocusWithScope(coroutineScope)
}