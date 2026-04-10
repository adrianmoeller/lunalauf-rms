package lunalauf.rms.centralapp.components.dialogs.details.runner

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Ban
import lunalauf.rms.centralapp.components.commons.IconSize
import lunalauf.rms.centralapp.components.commons.ListItemDivider
import lunalauf.rms.centralapp.components.commons.customScrollbarStyle
import lunalauf.rms.model.api.ModelState
import lunalauf.rms.model.internal.Team

@Composable
fun EditableTeamTile(
    modifier: Modifier = Modifier,
    team: Team?,
    onTeamChange: (Team?) -> Unit,
    modelState: ModelState.Loaded
) {
    var editDialogOpen by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(if (team != null) "Team:" else "Single runner")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            val name = if (team != null) {
                val teamName by team.name.collectAsState()
                teamName
            } else {
                "-"
            }

            Text(
                text = name,
                fontWeight = FontWeight.Bold
            )
            FilledTonalIconButton(
                onClick = { editDialogOpen = true }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Edit"
                )
            }
        }
    }
    EditTeamDialog(
        editDialogOpen = editDialogOpen,
        currentTeam = team,
        onClose = { editDialogOpen = false },
        onTeamChange = onTeamChange,
        modelState = modelState
    )
}

@Composable
fun EditTeamDialog(
    editDialogOpen: Boolean,
    currentTeam: Team?,
    onClose: () -> Unit,
    onTeamChange: (Team?) -> Unit,
    modelState: ModelState.Loaded
) {
    if (editDialogOpen) {
        val teams by modelState.event.teams.collectAsState()
        var selectedTeam by remember { mutableStateOf(currentTeam) }

        AlertDialog(
            modifier = Modifier
                .heightIn(max = 500.dp)
                .widthIn(max = 400.dp),
            title = { Text("Update Team") },
            text = {
                Box {
                    val listState = rememberLazyListState()
                    LazyColumn(
                        state = listState
                    ) {
                        item {
                            DialogTeamTile(
                                team = null,
                                selectedTeam = selectedTeam,
                                onSelect = { selectedTeam = null }
                            )
                        }
                        items(teams) { team ->
                            ListItemDivider(spacing = 10.dp)
                            DialogTeamTile(
                                team = team,
                                selectedTeam = selectedTeam,
                                onSelect = { selectedTeam = team }
                            )
                        }
                    }
                    VerticalScrollbar(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        adapter = rememberScrollbarAdapter(listState),
                        style = customScrollbarStyle
                    )
                }
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        onTeamChange(selectedTeam)
                        onClose()
                    },
                    enabled = selectedTeam != currentTeam
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onClose
                ) {
                    Text("Cancel")
                }
            },
            onDismissRequest = onClose
        )
    }
}

@Composable
private fun DialogTeamTile(
    team: Team?,
    selectedTeam: Team?,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .selectable(
                selected = selectedTeam == team,
                onClick = onSelect
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 10.dp)
                .padding(start = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (team != null) {
                val name by team.name.collectAsState()
                Text(name)
            } else {
                Icon(
                    modifier = Modifier.size(IconSize.small),
                    imageVector = FontAwesomeIcons.Solid.Ban,
                    contentDescription = null
                )
                Text(
                    text = "Single runner",
                    fontStyle = FontStyle.Italic
                )
            }
        }
        if (selectedTeam == team) {
            Row {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "${team?.name ?: "Single runner"} selected",
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(10.dp))
            }
        }
    }
}
