package lunalauf.rms.centralapp.ui.components.dialogs.createrunner

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import lunalauf.rms.centralapp.ui.components.dialogs.ScanChipField
import lunalauf.rms.centralapp.ui.components.dialogs.ScanChipScreenModel
import lunalauf.rms.modelapi.ModelState

data class ScanChipScreen(
    private val modelState: ModelState.Loaded,
    private val onDismissRequest: (Navigator) -> Unit
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
                                EnterNameScreen(
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