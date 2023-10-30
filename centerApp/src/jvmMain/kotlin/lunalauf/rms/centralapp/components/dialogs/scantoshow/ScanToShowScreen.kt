package lunalauf.rms.centralapp.components.dialogs.scantoshow

import LunaLaufLanguage.Runner
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import lunalauf.rms.centralapp.components.dialogs.ScanChipField
import lunalauf.rms.centralapp.components.dialogs.ScanChipScreenModel
import lunalauf.rms.modelapi.ModelState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanToShowScreen(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onShowRunnerDetails: (Runner) -> Unit,
    onCreateRunner: (ULong) -> Unit,
    modelState: ModelState.Loaded
) {
    val screenModel = remember { ScanChipScreenModel(modelState) }

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
                            onKnown = onShowRunnerDetails,
                            onUnknown = onCreateRunner
                        )
                        onDismissRequest()
                    }
                )
            }
        }
    }
}