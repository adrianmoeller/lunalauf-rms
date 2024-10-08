package lunalauf.rms.centralapp.components.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.regular.File
import compose.icons.fontawesomeicons.regular.FolderOpen
import lunalauf.rms.centralapp.components.commons.CustomSnackBarHost
import lunalauf.rms.centralapp.components.commons.IconSize

@Composable
fun NoFileOpenScreen(
    modifier: Modifier = Modifier,
    onNewClick: () -> Unit,
    onOpenClick: () -> Unit,
    snackBarHostState: SnackbarHostState
) {
    Scaffold(
        snackbarHost = { CustomSnackBarHost(snackBarHostState) }
    ) {
        Box(
            modifier = modifier
                .padding(it)
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
                        modifier = Modifier.padding(
                            top = 5.dp,
                            bottom = 6.dp
                        ),
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
                        modifier = Modifier.padding(
                            top = 5.dp,
                            bottom = 6.dp
                        ),
                        text = "Open",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}