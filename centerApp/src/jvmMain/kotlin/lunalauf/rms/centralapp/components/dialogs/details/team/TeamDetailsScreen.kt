package lunalauf.rms.centralapp.components.dialogs.details.team

import LunaLaufLanguage.Team
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import lunalauf.rms.centralapp.components.commons.DeleteElementDialog
import lunalauf.rms.centralapp.components.commons.EditableValueTile
import lunalauf.rms.centralapp.components.commons.FullScreenDialog
import lunalauf.rms.centralapp.components.commons.customScrollbarStyle
import lunalauf.rms.centralapp.components.dialogs.details.EditableContributionTile
import lunalauf.rms.centralapp.components.dialogs.details.StatTile
import lunalauf.rms.modelapi.ModelState

@Composable
fun TeamDetailsScreen(
    modifier: Modifier = Modifier,
    team: Team,
    onDismissRequest: () -> Unit,
    modelState: ModelState.Loaded,
    snackBarHostState: SnackbarHostState
) {
    val screenModel = remember { TeamDetailsScreenModel(modelState) }

    FullScreenDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = "Team: ${team.name}",
        maxWidth = 800.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(
                modifier = Modifier.weight(2f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(
                                horizontal = 15.dp,
                                vertical = 10.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            EditableValueTile(
                                name = "Name",
                                value = team.name,
                                onValueChange = { screenModel.updateName(team, it) },
                                parser = screenModel::validateName,
                                default = "",
                                editTitle = "Update name"
                            )
                            EditableContributionTile(
                                type = team.contribution,
                                amountFixed = team.amountFix,
                                amountPerRound = team.amountPerRound,
                                onValuesChange = { type, fixed, perRound ->
                                    screenModel.updateContribution(team, type, fixed, perRound)
                                }
                            )
                        }
                    }
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(
                                horizontal = 15.dp,
                                vertical = 10.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Text(
                                text = "Statistics",
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Box {
                                val listState = rememberLazyListState()
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(15.dp),
                                    state = listState
                                ) {
                                    items(screenModel.calcStats(team)) {
                                        StatTile(
                                            name = it.first,
                                            value = it.second
                                        )
                                    }
                                }
                                VerticalScrollbar(
                                    modifier = Modifier.align(Alignment.CenterEnd),
                                    adapter = rememberScrollbarAdapter(listState),
                                    style = customScrollbarStyle
                                )
                            }
                        }
                    }
                }
                DeleteElementDialog(
                    element = team,
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
                        Text("Delete team")
                    }
                }
            }
            // TODO tables
        }
    }
}