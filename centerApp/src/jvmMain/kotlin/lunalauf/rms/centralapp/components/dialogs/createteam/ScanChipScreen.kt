package lunalauf.rms.centralapp.components.dialogs.createteam

import LunaLaufLanguage.Team
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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