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
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ExclamationTriangle
import kotlinx.coroutines.launch
import lunalauf.rms.centralapp.ui.common.IconSize
import lunalauf.rms.centralapp.ui.components.ScanChipField
import lunalauf.rms.centralapp.ui.screenmodels.AbstractScreenModel
import lunalauf.rms.centralapp.ui.screenmodels.IdResult
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
    Navigator(
        ScanChipScreen(modelState, onDismissRequest)
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

data class ScanChipScreen(
    val modelState: ModelState.Loaded,
    val onDismissRequest: () -> Unit
) : Screen {
    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { ScanChipScreenModel(modelState) }
        val navigator = LocalNavigator.currentOrThrow

        var showError by remember { mutableStateOf(false) }

        Column (
            modifier = Modifier
                .padding(vertical = 20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ScanChipField(
                onNumberKeyEvent = screenModel::toIdBuffer,
                onEnterKeyEvent = {
                    when (val result = screenModel.processBufferedId()) {
                        IdResult.Error -> {
                            showError = true
                        }

                        is IdResult.Known -> {
                            // TODO show runner details
                            onDismissRequest()
                        }

                        is IdResult.Unknown -> {
                            navigator.push(
                                EnterNameScreen(
                                    modelState = modelState,
                                    id = result.id,
                                    onDismissRequest = onDismissRequest
                                )
                            )
                            showError = false
                        }
                    }
                }
            )
            if (showError) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            modifier = Modifier.size(IconSize.small),
                            imageVector = FontAwesomeIcons.Solid.ExclamationTriangle,
                            contentDescription = null
                        )
                        Text(IdResult.Error.message)
                    }
                }
            }
        }
    }
}

private data class EnterNameScreen(
    val modelState: ModelState.Loaded,
    val id: ULong,
    val onDismissRequest: () -> Unit
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
                                        onClose = onDismissRequest
                                    )
                                return@onPreviewKeyEvent true
                            }
                            return@onPreviewKeyEvent false
                        }
                        .focusRequester(focusRequester),
                    value = screenModel.name,
                    onValueChange = screenModel::updateName,
                    label = { Text("Name") },
                    isError = !screenModel.nameValid
                )
                Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = {
                            navigator.pop()
                        }
                    ) {
                        Text("Scan again")
                    }
                    FilledTonalButton(
                        onClick = {
                            screenModel.createRunner(
                                onClose = onDismissRequest
                            )
                        },
                        enabled = screenModel.nameValid
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