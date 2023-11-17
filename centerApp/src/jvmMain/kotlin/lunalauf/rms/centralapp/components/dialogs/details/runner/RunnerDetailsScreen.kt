package lunalauf.rms.centralapp.components.dialogs.details.runner

import LunaLaufLanguage.Round
import LunaLaufLanguage.Runner
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import lunalauf.rms.centralapp.components.commons.*
import lunalauf.rms.centralapp.components.commons.tables.ClickableTable
import lunalauf.rms.centralapp.components.dialogs.details.EditableContributionTile
import lunalauf.rms.centralapp.components.dialogs.details.StatTile
import lunalauf.rms.modelapi.ModelState

@Composable
fun RunnerDetailsScreen(
    modifier: Modifier = Modifier,
    runner: Runner,
    onDismissRequest: () -> Unit,
    modelState: ModelState.Loaded,
    snackBarHostState: SnackbarHostState
) {
    val screenModel = remember { RunnerDetailsScreenModel(modelState) }

    FullScreenDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = "Runner: ${if (runner.name.isNullOrBlank()) runner.id else runner.name}",
        maxWidth = 800.dp
    ) {
        val runnerDetailsCalc by screenModel.calcRunnerDetails(runner)
        if (runnerDetailsCalc is CalcResult.Available) {
            val runnerDetails = (runnerDetailsCalc as CalcResult.Available).result
            var deleteRoundState: DeleteRoundState by remember { mutableStateOf(DeleteRoundState.Closed) }

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
                                EditableIDTile(
                                    value = runner.id,
                                    onIdChange = { screenModel.updateID(runner, it) },
                                    modelState = modelState
                                )
                                EditableValueTile(
                                    name = "Name",
                                    value = runner.name,
                                    onValueChange = { screenModel.updateName(runner, it) },
                                    parser = screenModel::validateName,
                                    default = "",
                                    editTitle = "Update name"
                                )
                                EditableTeamTile(
                                    value = runner.team,
                                    onTeamChange = { screenModel.updateTeam(runner, it) },
                                    modelState = modelState
                                )
                                EditableContributionTile(
                                    type = runner.contribution,
                                    amountFixed = runner.amountFix,
                                    amountPerRound = runner.amountPerRound,
                                    onValuesChange = { type, fixed, perRound ->
                                        screenModel.updateContribution(runner, type, fixed, perRound)
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
                                        items(runnerDetails.stats) { (name, value) ->
                                            StatTile(
                                                name = name,
                                                value = value
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
                        element = runner,
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
                            Text("Delete runner")
                        }
                    }
                }
                ClickableTable(
                    modifier = Modifier.weight(1f),
                    header = listOf("Time", "Points"),
                    data = runnerDetails.roundsData,
                    weights = listOf(2.5f, 1f),
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Delete round",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = { deleteRoundState = DeleteRoundState.Open(it) }
                )
                val constDeleteRoundState = deleteRoundState
                if (constDeleteRoundState is DeleteRoundState.Open) {
                    DeleteElementDialog(
                        modifier = modifier,
                        element = constDeleteRoundState.round,
                        onClose = { deleteRoundState = DeleteRoundState.Closed },
                        onDeleted = {},
                        modelState = modelState,
                        snackBarHostState = snackBarHostState
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

private sealed class DeleteRoundState {
    data object Closed : DeleteRoundState()
    data class Open(val round: Round) : DeleteRoundState()
}
