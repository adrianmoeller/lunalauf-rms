package lunalauf.rms.centralapp.components.dialogs.details.team

import LunaLaufLanguage.FunfactorResult
import LunaLaufLanguage.Round
import LunaLaufLanguage.Runner
import LunaLaufLanguage.Team
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Bolt
import compose.icons.fontawesomeicons.solid.Running
import compose.icons.fontawesomeicons.solid.ShoePrints
import compose.icons.fontawesomeicons.solid.SlidersH
import lunalauf.rms.centralapp.components.commons.*
import lunalauf.rms.centralapp.components.commons.tables.ClickableTable
import lunalauf.rms.centralapp.components.dialogs.details.EditableContributionTile
import lunalauf.rms.centralapp.components.dialogs.details.StatTile
import lunalauf.rms.modelapi.ModelState

@Composable
fun TeamDetailsScreen(
    modifier: Modifier = Modifier,
    team: Team,
    onDismissRequest: () -> Unit,
    onAddRunnerRequest: (Team) -> Unit,
    onShowRunnerDetailsRequest: (Runner) -> Unit,
    modelState: ModelState.Loaded,
    snackBarHostState: SnackbarHostState
) {
    val screenModel = remember { TeamDetailsScreenModel(modelState) }

    FullScreenDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = "Team: ${team.name}",
        maxWidth = 1100.dp
    ) {
        val teamDetailsCalc by screenModel.calcTeamDetails(team)

        if (teamDetailsCalc is CalcResult.Available) {
            val teamDetails = (teamDetailsCalc as CalcResult.Available).result

            Row(
                modifier = Modifier.padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
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
                                        items(teamDetails.stats) { (name, value) ->
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
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
                                Spacer(Modifier.width(8.dp))
                                Text("Delete team")
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                onDismissRequest()
                                onAddRunnerRequest(team)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Add runners")
                        }
                    }
                }
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    var currentTab by remember { mutableStateOf(Tabs.Members) }
                    NavigationBar(
                        modifier = Modifier.clip(MaterialTheme.shapes.medium)
                    ) {
                        TabsNavigationBarItem(
                            tab = Tabs.Members,
                            currentTab = currentTab,
                            onClick = { currentTab = it }
                        )
                        TabsNavigationBarItem(
                            tab = Tabs.Rounds,
                            currentTab = currentTab,
                            onClick = { currentTab = it }
                        )
                        TabsNavigationBarItem(
                            tab = Tabs.Funfactors,
                            currentTab = currentTab,
                            onClick = { currentTab = it }
                        )
                    }
                    Spacer(Modifier.height(15.dp))
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        currentTab.content(
                            modelState,
                            teamDetails,
                            {
                                onDismissRequest()
                                onShowRunnerDetailsRequest(it)
                            },
                            snackBarHostState
                        )
                    }
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

@Composable
private fun RowScope.TabsNavigationBarItem(
    tab: Tabs,
    currentTab: Tabs,
    onClick: (Tabs) -> Unit
) {
    NavigationBarItem(
        label = { Text(tab.title) },
        icon = {
            Icon(
                modifier = Modifier.size(IconSize.small),
                imageVector = tab.icon,
                contentDescription = null
            )
        },
        selected = currentTab == tab,
        onClick = { onClick(tab) }
    )
}

private enum class Tabs(
    val title: String,
    val icon: ImageVector,
    val content: @Composable (
        ModelState.Loaded,
        TeamDetails,
        (Runner) -> Unit,
        SnackbarHostState
    ) -> Unit
) {
    Members(
        title = "Members",
        icon = FontAwesomeIcons.Solid.Running,
        content = { _, details, onShowRunnerDetailsRequest, _ ->
            val data = details.membersData
            ClickableTable(
                header = listOf("ID", "Name", "Points"),
                data = data,
                weights = listOf(3f, 5f, 2f),
                icon = {
                    Icon(
                        modifier = Modifier.size(IconSize.small - 2.dp),
                        imageVector = FontAwesomeIcons.Solid.SlidersH,
                        contentDescription = "Show runner details",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                },
                onClick = onShowRunnerDetailsRequest
            )
        }
    ),
    Rounds(
        title = "Rounds",
        icon = FontAwesomeIcons.Solid.ShoePrints,
        content = { modelState, details, _, snackBarHostState ->
            var deleteRoundState: DeleteRoundState by remember { mutableStateOf(DeleteRoundState.Closed) }
            val data = details.roundsData
            ClickableTable(
                header = listOf("Time", "Runner", "Points"),
                data = data,
                weights = listOf(3f, 5f, 2f),
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
                    element = constDeleteRoundState.round,
                    onClose = { deleteRoundState = DeleteRoundState.Closed },
                    onDeleted = {},
                    modelState = modelState,
                    snackBarHostState = snackBarHostState
                )
            }
        }
    ),
    Funfactors(
        title = "Funfactor results",
        icon = FontAwesomeIcons.Solid.Bolt,
        content = { modelState, details, _, snackBarHostState ->
            var deleteFunfactorResultState: DeleteFunfactorResultState by remember {
                mutableStateOf(DeleteFunfactorResultState.Closed)
            }
            val data = details.funfactorResultsData
            ClickableTable(
                header = listOf("Time", "Funfactor", "Points"),
                data = data,
                weights = listOf(2f, 5f, 1f),
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete round",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                },
                onClick = { deleteFunfactorResultState = DeleteFunfactorResultState.Open(it) }
            )
            val constDeleteFunfactorResultState = deleteFunfactorResultState
            if (constDeleteFunfactorResultState is DeleteFunfactorResultState.Open) {
                DeleteElementDialog(
                    element = constDeleteFunfactorResultState.funfactorResult,
                    onClose = { deleteFunfactorResultState = DeleteFunfactorResultState.Closed },
                    onDeleted = {},
                    modelState = modelState,
                    snackBarHostState = snackBarHostState
                )
            }
        }
    )
}

private sealed class DeleteRoundState {
    data object Closed : DeleteRoundState()
    data class Open(val round: Round) : DeleteRoundState()
}

private sealed class DeleteFunfactorResultState {
    data object Closed : DeleteFunfactorResultState()
    data class Open(val funfactorResult: FunfactorResult) : DeleteFunfactorResultState()
}
