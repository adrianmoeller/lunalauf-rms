package lunalauf.rms.centralapp.ui.components.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.regular.File
import compose.icons.fontawesomeicons.regular.FolderOpen
import lunalauf.rms.centralapp.ui.components.commons.IconSize

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
                Icon(
                    modifier = Modifier.size(IconSize.medium),
                    imageVector = FontAwesomeIcons.Regular.File,
                    contentDescription = null
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    modifier = Modifier.padding(bottom = 1.dp),
                    text = "New",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            OutlinedButton(
                onClick = onOpenClick
            ) {
                Icon(
                    modifier = Modifier.size(IconSize.medium),
                    imageVector = FontAwesomeIcons.Regular.FolderOpen,
                    contentDescription = null
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    modifier = Modifier.padding(bottom = 1.dp),
                    text = "Open",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}