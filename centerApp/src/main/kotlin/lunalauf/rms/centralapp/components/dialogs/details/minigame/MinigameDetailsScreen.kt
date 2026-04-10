package lunalauf.rms.centralapp.components.dialogs.details.minigame

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import lunalauf.rms.centralapp.components.commons.DeleteElementDialog
import lunalauf.rms.centralapp.components.commons.EditableValueTile
import lunalauf.rms.centralapp.components.commons.FullScreenDialog
import lunalauf.rms.model.api.ModelState
import lunalauf.rms.model.internal.Minigame

@Composable
fun MinigameDetailsScreen(
    modifier: Modifier = Modifier,
    minigame: Minigame,
    onDismissRequest: () -> Unit,
    modelState: ModelState.Loaded,
    snackBarHostState: SnackbarHostState
) {
    val screenModel = remember { MinigameDetailsScreenModel(modelState) }

    val id by minigame.id.collectAsState()
    val name by minigame.name.collectAsState()

    FullScreenDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = "Minigame: ${minigame.name}",
        maxWidth = 500.dp,
        maxHeight = 500.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 15.dp,
                        vertical = 10.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    EditableValueTile(
                        name = "ID",
                        value = id,
                        onValueChange = { screenModel.updateId(minigame, it) },
                        parser = screenModel::validateId,
                        default = 0,
                        editTitle = "Update ID"
                    )
                    EditableValueTile(
                        name = "Name",
                        value = name,
                        onValueChange = { screenModel.updateName(minigame, it) },
                        parser = screenModel::validateName,
                        default = "",
                        editTitle = "Update name"
                    )
                }
            }
            DeleteElementDialog(
                element = minigame,
                onDeleted = onDismissRequest,
                modelState = modelState,
                snackBarHostState = snackBarHostState
            ) {
                FilledTonalButton(
                    onClick = it,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Delete minigame")
                }
            }
        }
    }
}