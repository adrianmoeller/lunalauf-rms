package lunalauf.rms.centralapp.components.dialogs.create.team

import LunaLaufLanguage.Runner
import LunaLaufLanguage.Team
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import lunalauf.rms.modelapi.ModelState

data class ExistingRunnerScreen(
    private val modelState: ModelState.Loaded,
    private val team: Team,
    private val runner: Runner,
    private val onDismissRequest: (Navigator) -> Unit,
    private val snackBarHostState: SnackbarHostState
) : Screen {
    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel {
            ExistingRunnerScreenModel(
                modelState = modelState,
                team = team,
                runner = runner,
                snackBarHostState = snackBarHostState
            )
        }
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
                    Text(
                        text = screenModel.displayMessage,
                        style = MaterialTheme.typography.titleMedium
                    )
                    OutlinedCard {
                        Row(
                            modifier = Modifier.padding(
                                vertical = 10.dp,
                                horizontal = 15.dp
                            ),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text("ID:\nName:")
                            Text("${runner.id}\n${runner.name ?: "-"}")
                        }
                    }
                }
                FilledTonalButton(
                    modifier = Modifier.align(Alignment.End),
                    onClick = {
                        screenModel.addRunnerToTeam(
                            onBack = { navigator.pop() },
                            onClose = { onDismissRequest(navigator) }
                        )
                    },
                    enabled = !screenModel.processing
                ) {
                    Text("Add to team")
                }
            }
            if (screenModel.processing)
                CircularProgressIndicator()
        }
    }
}