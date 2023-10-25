package lunalauf.rms.centralapp.components.dialogs.createteam

import LunaLaufLanguage.Team
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import lunalauf.rms.centralapp.components.dialogs.ScanChipField
import lunalauf.rms.centralapp.components.dialogs.ScanChipScreenModel
import lunalauf.rms.modelapi.ModelState

data class ScanChipScreen(
    private val modelState: ModelState.Loaded,
    private val team: Team,
    private val onDismissRequest: (Navigator) -> Unit,
    private val snackBarHostState: SnackbarHostState
) : Screen {
    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { ScanChipScreenModel(modelState) }
        var alreadyMember by remember { mutableStateOf(false) }
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                if (alreadyMember) {
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
                            text = "This runner is already a team member",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                ScanChipField(
                    showError = screenModel.showError,
                    onNumberKeyEvent = screenModel::toIdBuffer,
                    onEnterKeyEvent = {
                        screenModel.processBufferedId(
                            onKnown = {
                                if (it.team == team) {
                                    alreadyMember = true
                                } else {
                                    navigator.push(
                                        ExistingRunnerScreen(
                                            modelState = modelState,
                                            team = team,
                                            runner = it,
                                            onDismissRequest = onDismissRequest,
                                            snackBarHostState = snackBarHostState
                                        )
                                    )
                                    alreadyMember = false
                                }
                            },
                            onUnknown = {
                                navigator.push(
                                    EnterRunnerNameScreen(
                                        modelState = modelState,
                                        team = team,
                                        id = it,
                                        onDismissRequest = onDismissRequest,
                                        snackBarHostState = snackBarHostState
                                    )
                                )
                                alreadyMember = false
                            }
                        )
                    }
                )
            }
            FilledTonalButton(
                modifier = Modifier.align(Alignment.End),
                onClick = { onDismissRequest(navigator) }
            ) {
                Text("Done")
            }
        }
    }
}