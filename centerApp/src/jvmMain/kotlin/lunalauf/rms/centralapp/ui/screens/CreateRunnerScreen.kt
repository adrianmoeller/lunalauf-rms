package lunalauf.rms.centralapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import lunalauf.rms.centralapp.ui.components.ScanChipField
import lunalauf.rms.centralapp.ui.screenmodels.AbstractScreenModel
import lunalauf.rms.centralapp.ui.screenmodels.ScanChipScreenModel
import lunalauf.rms.centralapp.util.InputValidator
import lunalauf.rms.modelapi.CreateRunnerResult
import lunalauf.rms.modelapi.ModelState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRunnerScreen(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    modelState: ModelState.Loaded
) {
    val onDismissRequestClear: (Navigator) -> Unit = {
        onDismissRequest()
        it.popAll()
    }
    val startScreen = ScanChipRunnerScreen(modelState, onDismissRequestClear)

    Navigator(
        listOf(
            DummyScreen(startScreen),
            startScreen
        )
    ) {
        ModalBottomSheet(
            modifier = modifier,
            onDismissRequest = {
                onDismissRequest()
                it.popAll()
            },
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
                    text = "Create single runner",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(20.dp))
                CurrentScreen()
            }
        }
    }
}

private data class ScanChipRunnerScreen(
    val modelState: ModelState.Loaded,
    val onDismissRequest: (Navigator) -> Unit
) : Screen {
    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { ScanChipScreenModel(modelState) }
        val navigator = LocalNavigator.currentOrThrow

        Column(
            modifier = Modifier
                .padding(vertical = 20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScanChipField(
                showError = screenModel.showError,
                onNumberKeyEvent = screenModel::toIdBuffer,
                onEnterKeyEvent = {
                    screenModel.processBufferedId(
                        onKnown = {
                            // TODO show runner details
                            onDismissRequest(navigator)
                        },
                        onUnknown = {
                            navigator.push(
                                EnterRunnerNameScreen(
                                    modelState = modelState,
                                    id = it,
                                    onDismissRequest = onDismissRequest
                                )
                            )
                        }
                    )
                }
            )
        }
    }
}

private data class EnterRunnerNameScreen(
    val modelState: ModelState.Loaded,
    val id: ULong,
    val onDismissRequest: (Navigator) -> Unit
) : Screen {
    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel {
            EnterNameScreenModel(
                modelState = modelState,
                id = id
            )
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
                Text(
                    text = "ID: $id",
                    style = MaterialTheme.typography.titleMedium
                )
                OutlinedTextField(
                    modifier = Modifier
                        .onPreviewKeyEvent {
                            if (it.key == Key.Enter) {
                                if (it.type == KeyEventType.KeyUp && screenModel.nameValid)
                                    screenModel.createRunner(
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
                Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = {
                            navigator.pop()
                        },
                        enabled = !screenModel.processing
                    ) {
                        Text("Scan again")
                    }
                    FilledTonalButton(
                        onClick = {
                            screenModel.createRunner(
                                onClose = { onDismissRequest(navigator) }
                            )
                        },
                        enabled = screenModel.nameValid && !screenModel.processing
                    ) {
                        Text("Create")
                    }
                }
            }
            if (screenModel.processing)
                CircularProgressIndicator()
        }

        coroutineScope.launch {
            try {
                focusRequester.requestFocus()
            } catch (_: Exception) {
            }
        }
    }
}

private class EnterNameScreenModel(
    modelState: ModelState.Loaded,
    private val id: ULong
) : ScreenModel, AbstractScreenModel() {
    val modelAPI = modelState.modelAPI

    var name by mutableStateOf("")
        private set
    var nameValid by mutableStateOf(true)
        private set
    var processing by mutableStateOf(false)
        private set

    fun updateName(name: String) {
        nameValid = InputValidator.validateName(name)
        this.name = name
    }

    fun createRunner(onClose: () -> Unit) {
        processing = true
        launchInDefaultScope {
            when (modelAPI.createRunner(id, name)) {
                is CreateRunnerResult.Created -> {
                    // TODO
                }

                is CreateRunnerResult.Exists -> {
                    // TODO
                }
            }
            onClose()
        }
    }
}