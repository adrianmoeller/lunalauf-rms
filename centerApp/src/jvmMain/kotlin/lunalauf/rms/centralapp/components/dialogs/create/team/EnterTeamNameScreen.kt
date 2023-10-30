package lunalauf.rms.centralapp.components.dialogs.create.team

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
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import lunalauf.rms.centralapp.components.commons.tryRequestFocusWithScope
import lunalauf.rms.modelapi.ModelState

data class EnterTeamNameScreen(
    private val modelState: ModelState.Loaded,
    private val onDismissRequest: (Navigator) -> Unit,
    val snackBarHostState: SnackbarHostState
) : Screen {
    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel {
            EnterTeamNameScreenModel(modelState, snackBarHostState)
        }
        val coroutineScope = rememberCoroutineScope()
        val focusRequester = remember { FocusRequester() }
        val navigator = LocalNavigator.currentOrThrow

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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    if (screenModel.nameDuplicate) {
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
                                text = "This team name already exists",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    OutlinedTextField(
                        modifier = Modifier
                            .onPreviewKeyEvent {
                                if (it.key == Key.Enter) {
                                    if (it.type == KeyEventType.KeyUp && screenModel.nameValid)
                                        screenModel.createTeam(
                                            onCreated = { team ->
                                                navigator.push(
                                                    ScanChipScreen(
                                                        modelState = modelState,
                                                        team = team,
                                                        onDismissRequest = onDismissRequest,
                                                        snackBarHostState = snackBarHostState
                                                    )
                                                )
                                            },
                                            onClose = { onDismissRequest(navigator) }
                                        )
                                    return@onPreviewKeyEvent true
                                }
                                return@onPreviewKeyEvent false
                            }
                            .focusRequester(focusRequester),
                        value = screenModel.name,
                        onValueChange = screenModel::updateName,
                        label = { Text("Name") },
                        isError = !screenModel.nameValid,
                        enabled = !screenModel.processing
                    )
                }
                FilledTonalButton(
                    modifier = Modifier.align(Alignment.End),
                    onClick = {
                        screenModel.createTeam(
                            onCreated = {
                                navigator.push(
                                    ScanChipScreen(
                                        modelState = modelState,
                                        team = it,
                                        onDismissRequest = onDismissRequest,
                                        snackBarHostState = snackBarHostState
                                    )
                                )
                            },
                            onClose = { onDismissRequest(navigator) }
                        )
                    },
                    enabled = screenModel.nameValid && !screenModel.processing
                ) {
                    Text("Create")
                }
            }
            if (screenModel.processing)
                CircularProgressIndicator()
        }

        focusRequester.tryRequestFocusWithScope(coroutineScope)
    }
}