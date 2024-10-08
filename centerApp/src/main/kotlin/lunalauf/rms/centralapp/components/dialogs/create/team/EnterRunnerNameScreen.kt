package lunalauf.rms.centralapp.components.dialogs.create.team

import LunaLaufLanguage.Team
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import lunalauf.rms.centralapp.components.commons.tryRequestFocus
import lunalauf.rms.modelapi.ModelState

data class EnterRunnerNameScreen(
    private val modelState: ModelState.Loaded,
    private val team: Team,
    private val id: Long,
    private val onDismissRequest: (Navigator) -> Unit,
    private val snackBarHostState: SnackbarHostState
) : Screen {
    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel {
            EnterRunnerNameScreenModel(
                modelState = modelState,
                id = id,
                team = team,
                snackBarHostState = snackBarHostState
            )
        }
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
                                    screenModel.createRunnerAndAddToTeam(
                                        onClose = { onDismissRequest(navigator) },
                                        onBack = { navigator.pop() }
                                    )
                                return@onPreviewKeyEvent true
                            }
                            return@onPreviewKeyEvent false
                        }
                        .focusRequester(focusRequester),
                    value = screenModel.name,
                    onValueChange = screenModel::updateName,
                    label = { Text("Runner's name") },
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
                            screenModel.createRunnerAndAddToTeam(
                                onClose = { onDismissRequest(navigator) },
                                onBack = { navigator.pop() }
                            )
                        },
                        enabled = screenModel.nameValid && !screenModel.processing
                    ) {
                        Text("Create & add to team")
                    }
                }
            }
            if (screenModel.processing)
                CircularProgressIndicator()
        }

        focusRequester.tryRequestFocus()
    }
}