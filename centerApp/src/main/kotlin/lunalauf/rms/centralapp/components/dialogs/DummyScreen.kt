package lunalauf.rms.centralapp.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

/**
 * This is a HOTFIX:
 * Voyager for Compose always remembers the state of the ScreenModel
 * of the first screen in the stack, even after pop()-operation.
 * We prepend a dummy screen to the deep link to avoid this.
 * Important is that the user should not be able to back press to the dummy screen.
 */
data class DummyScreen(
    val nextScreen: Screen
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                modifier = Modifier
                    .padding(10.dp),
                onClick = {
                    navigator.push(nextScreen)
                }
            ) {
                Text("Start")
            }
        }
        navigator.push(nextScreen)
    }
}