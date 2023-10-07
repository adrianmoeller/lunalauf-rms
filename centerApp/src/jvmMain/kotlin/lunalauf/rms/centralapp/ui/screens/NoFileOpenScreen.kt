package lunalauf.rms.centralapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NoFileOpenScreen(
    modifier: Modifier = Modifier,
    onNewClick: () -> Unit,
    onOpenClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            OutlinedButton(
                onClick = onNewClick
            ) {
                Text(
                    text = "New",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            OutlinedButton(
                onClick = onOpenClick
            ) {
                Text(
                    text = "Open",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}