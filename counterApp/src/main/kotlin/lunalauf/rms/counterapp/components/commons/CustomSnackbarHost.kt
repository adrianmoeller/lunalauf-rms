package lunalauf.rms.counterapp.components.commons

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable

@Composable
fun CustomSnackBarHost(
    snackbarHostState: SnackbarHostState
) {
    SnackbarHost(snackbarHostState) {
        val visuals = it.visuals
        if (visuals is CustomSnackBarVisuals && visuals.isError) {
            Snackbar(
                snackbarData = it,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.error,
                actionColor = MaterialTheme.colorScheme.onErrorContainer,
                actionContentColor = MaterialTheme.colorScheme.onErrorContainer,
                dismissActionContentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        } else {
            Snackbar(it)
        }
    }
}