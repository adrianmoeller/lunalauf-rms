package lunalauf.rms.centralapp.ui.screens

import LunaLaufLanguage.Team
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
import lunalauf.rms.modelapi.CreateTeamResult
import lunalauf.rms.modelapi.ModelState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTeamScreen(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    modelState: ModelState.Loaded
) {
    val onDismissRequestClear: (Navigator) -> Unit = {
        onDismissRequest()
        it.popAll()
    }
    val startScreen = EnterTeamNameScreen(modelState, onDismissRequestClear)

    Navigator(
        listOf(
            DummyScreen(startScreen), // HOTFIX to clear screen model state
            startScreen
        )
    ) {
        ModalBottomSheet(
            modifier = modifier,
            onDismissRequest = { onDismissRequestClear(it) },
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
                    text = "Create team",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(20.dp))
                CurrentScreen()
            }
        }
    }
}

private data class EnterTeamNameScreen(
    val modelState: ModelState.Loaded,
    val onDismissRequest: (Navigator) -> Unit
) : Screen {
    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel {
            EnterTeamNameScreenModel(modelState)
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
                OutlinedTextField(
                    modifier = Modifier
                        .onPreviewKeyEvent {
                            if (it.key == Key.Enter) {
                                if (it.type == KeyEventType.KeyUp && screenModel.nameValid)
                                    screenModel.createTeam(
                                        onCreated = { team ->
                                            navigator.push(
                                                ScanChipTeamScreen(
                                                    modelState = modelState,
                                                    team = team,
                                                    onDismissRequest = onDismissRequest
                                                )
                                            )
                                        }
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
                FilledTonalButton(
                    modifier = Modifier.align(Alignment.End),
                    onClick = {
                        screenModel.createTeam(
                            onCreated = {
                                navigator.push(
                                    ScanChipTeamScreen(
                                        modelState = modelState,
                                        team = it,
                                        onDismissRequest = onDismissRequest
                                    )
                                )
                            }
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

        coroutineScope.launch {
            try {
                focusRequester.requestFocus()
            } catch (_: Exception) {
            }
        }
    }
}

private class EnterTeamNameScreenModel(
    modelState: ModelState.Loaded
) : ScreenModel, AbstractScreenModel() {
    val modelAPI = modelState.modelAPI

    var name by mutableStateOf("")
        private set
    var nameValid by mutableStateOf(false)
        private set
    var processing by mutableStateOf(false)
        private set

    fun updateName(name: String) {
        nameValid = name.isNotBlank() && InputValidator.validateName(name)
        this.name = name
    }

    fun createTeam(
        onCreated: (Team) -> Unit
    ) {
        processing = true
        launchInDefaultScope {
            when (val result = modelAPI.createTeam(name)) {
                is CreateTeamResult.Created -> {
                    onCreated(result.team)
                }

                is CreateTeamResult.Exists -> {
                    // TODO
                }

                CreateTeamResult.BlankName -> {
                    // TODO
                }
            }
        }
    }
}

private data class ScanChipTeamScreen(
    private val modelState: ModelState.Loaded,
    private val team: Team,
    private val onDismissRequest: (Navigator) -> Unit
) : Screen {
    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { ScanChipScreenModel(modelState) }
        val navigator = LocalNavigator.currentOrThrow

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Add runner to team '${team.name}':",
                style = MaterialTheme.typography.titleMedium
            )
            ScanChipField(
                showError = screenModel.showError,
                onNumberKeyEvent = screenModel::toIdBuffer,
                onEnterKeyEvent = {
                    screenModel.processBufferedId(
                        onKnown = {
                            // TODO
                        },
                        onUnknown = {
                            // TODO
                        }
                    )
                }
            )
            FilledTonalButton(
                modifier = Modifier.align(Alignment.End),
                onClick = { onDismissRequest(navigator) }
            ) {
                Text("Done")
            }
        }
    }
}
