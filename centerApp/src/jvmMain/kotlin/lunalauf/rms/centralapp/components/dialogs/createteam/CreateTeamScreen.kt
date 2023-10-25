package lunalauf.rms.centralapp.components.dialogs.createteam

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import lunalauf.rms.centralapp.components.dialogs.DummyScreen
import lunalauf.rms.modelapi.ModelState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTeamScreen(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    modelState: ModelState.Loaded,
    snackBarHostState: SnackbarHostState
) {
    val onDismissRequestClear: (Navigator) -> Unit = {
        onDismissRequest()
        it.popAll()
    }
    val startScreen = EnterTeamNameScreen(modelState, onDismissRequestClear, snackBarHostState)

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
                    text = "Create team",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(20.dp))
                CurrentScreen()
            }
        }
    }
}
