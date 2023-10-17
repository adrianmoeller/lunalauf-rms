package lunalauf.rms.centralapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import lunalauf.rms.modelapi.ModelState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTeamScreen(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    modelState: ModelState
) {
    Navigator(
        EnterTeamNameScreen(modelState, onDismissRequest)
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
    val modelState: ModelState,
    val onDismissRequest: () -> Unit
) : Screen {
    @Composable
    override fun Content() {
        TODO("Not yet implemented")
    }
}
