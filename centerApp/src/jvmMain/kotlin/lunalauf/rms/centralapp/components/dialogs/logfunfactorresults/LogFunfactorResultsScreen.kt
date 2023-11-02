package lunalauf.rms.centralapp.components.dialogs.logfunfactorresults

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import lunalauf.rms.centralapp.components.dialogs.DummyScreen
import lunalauf.rms.modelapi.ModelState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogFunfactorResultsScreen(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    modelState: ModelState.Loaded,
    snackBarHostState: SnackbarHostState
) {
    val onDismissRequestClear: (Navigator) -> Unit = {
        onDismissRequest()
        it.popAll()
    }
    val startScreen = ChooseFunfactorScreen(
        modelState = modelState,
        onDismissRequest = onDismissRequestClear,
        snackBarHostState = snackBarHostState
    )

    Navigator(
        listOf(
            DummyScreen(startScreen),
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
                    text = "Log funfactor results",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(20.dp))
                CurrentScreen()
            }
        }
    }
}