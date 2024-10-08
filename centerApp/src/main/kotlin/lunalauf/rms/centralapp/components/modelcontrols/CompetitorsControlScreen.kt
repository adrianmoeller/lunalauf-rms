package lunalauf.rms.centralapp.components.modelcontrols

import LunaLaufLanguage.Runner
import LunaLaufLanguage.Team
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import compose.icons.EvaIcons
import compose.icons.FontAwesomeIcons
import compose.icons.evaicons.Outline
import compose.icons.evaicons.outline.Expand
import compose.icons.evaicons.outline.Shake
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.SlidersH
import kotlinx.coroutines.launch
import lunalauf.rms.centralapp.components.commons.*
import lunalauf.rms.centralapp.components.commons.tables.ClickableTable
import lunalauf.rms.centralapp.components.dialogs.create.runner.CreateRunnerScreen
import lunalauf.rms.centralapp.components.dialogs.create.team.CreateTeamScreen
import lunalauf.rms.centralapp.components.dialogs.details.runner.RunnerDetailsScreen
import lunalauf.rms.centralapp.components.dialogs.details.team.TeamDetailsScreen
import lunalauf.rms.centralapp.components.dialogs.scantoshow.ScanToShowScreen
import lunalauf.rms.modelapi.ModelState

@Composable
fun CompetitorsControlScreen(
    modifier: Modifier = Modifier,
    modelState: ModelState.Loaded,
    snackBarHostState: SnackbarHostState
) {
    val screenModel = remember { CompetitorsControlScreenModel(modelState) }
    val scope = rememberCoroutineScope()
    val teamsState by modelState.teams.collectAsState()
    val runnersState by modelState.runners.collectAsState()

    var teamsTableOpen by remember { mutableStateOf(false) }
    var runnersTableOpen by remember { mutableStateOf(false) }

    var createTeamOpen by remember { mutableStateOf(false) }
    var createRunnerOpen by remember { mutableStateOf(false) }
    var scanToShowOpen by remember { mutableStateOf(false) }
    var knownID: Long? by remember { mutableStateOf(null) }
    var knownTeam: Team? by remember { mutableStateOf(null) }

    var teamDetailsStatus: TeamDetailsStatus by remember { mutableStateOf(TeamDetailsStatus.Closed) }
    var runnerDetailsStatus: RunnerDetailsStatus by remember { mutableStateOf(RunnerDetailsStatus.Closed) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .widthIn(max = 800.dp)
                .weight(1f, false),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            CompetitorsControlCard(
                modifier = Modifier.weight(1f),
                title = "Teams",
                onExpand = { teamsTableOpen = true },
                createLabel = "Create team",
                onCreate = { createTeamOpen = true }
            ) {
                itemsIndexed(teamsState.teams) { index, team ->
                    TeamTile(
                        modifier = Modifier.fillMaxWidth(),
                        teamName = team.name ?: "",
                        onClick = { teamDetailsStatus = TeamDetailsStatus.Open(team) }
                    )
                    if (index < teamsState.teams.lastIndex)
                        ListItemDivider(spacing = 10.dp)
                }
            }
            CompetitorsControlCard(
                modifier = Modifier.weight(1f),
                title = "Single runners",
                onExpand = { runnersTableOpen = true },
                createLabel = "Create single runner",
                onCreate = { createRunnerOpen = true }
            ) {
                val singleRunners = runnersState.runners.filter { it.team == null }
                itemsIndexed(singleRunners) { index, runner ->
                    val runnerName = if (runner.name.isNullOrBlank()) runner.id.toString() else runner.name
                    RunnerTile(
                        modifier = Modifier.fillMaxWidth(),
                        runnerName = runnerName,
                        onClick = { runnerDetailsStatus = RunnerDetailsStatus.Open(runner) }
                    )
                    if (index < singleRunners.lastIndex)
                        ListItemDivider(spacing = 10.dp)
                }
            }
        }
        OutlinedButton(
            modifier = Modifier.padding(bottom = 20.dp),
            onClick = { scanToShowOpen = true }
        ) {
            Icon(
                modifier = Modifier.size(IconSize.large + 5.dp),
                imageVector = EvaIcons.Outline.Shake,
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text(
                modifier = Modifier.padding(bottom = 3.dp),
                text = "Scan",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }

    if (teamsTableOpen) {
        ExpandedTeamsTable(
            onDismissRequest = { teamsTableOpen = false },
            onShowTeamDetailsRequest = { teamDetailsStatus = TeamDetailsStatus.Open(it) },
            screenModel = screenModel
        )
    }

    if (runnersTableOpen) {
        ExpandedSingleRunnersTable(
            onDismissRequest = { runnersTableOpen = false },
            onShowRunnerDetailsRequest = { runnerDetailsStatus = RunnerDetailsStatus.Open(it) },
            screenModel = screenModel
        )
    }

    if (createTeamOpen) {
        CreateTeamScreen(
            onDismissRequest = { createTeamOpen = false },
            knownTeam = knownTeam,
            resetKnownTeam = { knownTeam = null },
            modelState = modelState,
            snackBarHostState = snackBarHostState
        )
    }

    if (createRunnerOpen) {
        CreateRunnerScreen(
            onDismissRequest = { createRunnerOpen = false },
            onShowRunnerDetails = {
                scope.launch {
                    val result = snackBarHostState.showSnackbar(
                        message = "A runner with this ID already exists",
                        actionLabel = "Show",
                        withDismissAction = true,
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed)
                        runnerDetailsStatus = RunnerDetailsStatus.Open(it)
                }
            },
            knownID = knownID,
            resetKnownID = { knownID = null },
            modelState = modelState,
            snackBarHostState = snackBarHostState
        )
    }

    if (scanToShowOpen) {
        ScanToShowScreen(
            onDismissRequest = { scanToShowOpen = false },
            onShowRunnerDetails = { runnerDetailsStatus = RunnerDetailsStatus.Open(it) },
            onCreateRunner = {
                scope.launch {
                    val result = snackBarHostState.showSnackbar(
                        message = "A runner with this ID does not exists",
                        actionLabel = "Create",
                        withDismissAction = true,
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        knownID = it
                        createRunnerOpen = true
                    }
                }
            },
            modelState = modelState
        )
    }

    if (teamDetailsStatus is TeamDetailsStatus.Open) {
        val team = (teamDetailsStatus as TeamDetailsStatus.Open).team
        TeamDetailsScreen(
            team = remember(teamsState) { team },
            onDismissRequest = { teamDetailsStatus = TeamDetailsStatus.Closed },
            onAddRunnerRequest = {
                knownTeam = it
                createTeamOpen = true
            },
            onShowRunnerDetailsRequest = { runnerDetailsStatus = RunnerDetailsStatus.Open(it) },
            modelState = modelState,
            snackBarHostState = snackBarHostState
        )
    }

    if (runnerDetailsStatus is RunnerDetailsStatus.Open) {
        val runner = (runnerDetailsStatus as RunnerDetailsStatus.Open).runner
        RunnerDetailsScreen(
            runner = remember(runnersState) { runner },
            onDismissRequest = { runnerDetailsStatus = RunnerDetailsStatus.Closed },
            modelState = modelState,
            snackBarHostState = snackBarHostState
        )
    }
}

@Composable
private fun CompetitorsControlCard(
    modifier: Modifier = Modifier,
    title: String,
    onExpand: () -> Unit,
    createLabel: String,
    onCreate: () -> Unit,
    content: LazyListScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedCard(
            modifier = Modifier.weight(1f, false)
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 15.dp,
                    vertical = 10.dp
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(
                        onClick = onExpand
                    ) {
                        Icon(
                            imageVector = EvaIcons.Outline.Expand,
                            contentDescription = "Expand ${title.lowercase()} view"
                        )
                    }
                }
                Box {
                    val density = LocalDensity.current
                    val listState = rememberLazyListState()
                    var listHeight by remember { mutableStateOf(0.dp) }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned {
                                listHeight = with(density) {
                                    it.size.height.toDp()
                                }
                            },
                        state = listState,
                        content = content
                    )
                    VerticalScrollbar(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .height(listHeight),
                        adapter = rememberScrollbarAdapter(listState),
                        style = customScrollbarStyle
                    )
                }
            }
        }
        OutlinedIconButton(
            onClick = onCreate
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = createLabel
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TeamTile(
    modifier: Modifier = Modifier,
    teamName: String,
    onClick: () -> Unit
) {
    var hovered by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .onPointerEvent(PointerEventType.Enter) { hovered = true }
            .onPointerEvent(PointerEventType.Exit) { hovered = false }
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 10.dp)
                .padding(start = 10.dp),
            text = teamName
        )
        if (hovered) {
            Row {
                Icon(
                    modifier = Modifier.size(IconSize.small - 2.dp),
                    imageVector = FontAwesomeIcons.Solid.SlidersH,
                    contentDescription = "Show team details",
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(10.dp))
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun RunnerTile(
    modifier: Modifier = Modifier,
    runnerName: String,
    onClick: () -> Unit
) {
    var hovered by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .onPointerEvent(PointerEventType.Enter) { hovered = true }
            .onPointerEvent(PointerEventType.Exit) { hovered = false }
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 10.dp)
                .padding(start = 10.dp),
            text = runnerName
        )
        if (hovered) {
            Row {
                Icon(
                    modifier = Modifier.size(IconSize.small - 2.dp),
                    imageVector = FontAwesomeIcons.Solid.SlidersH,
                    contentDescription = "Show runner details",
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(10.dp))
            }
        }
    }
}

@Composable
private fun ExpandedTeamsTable(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onShowTeamDetailsRequest: (Team) -> Unit,
    screenModel: CompetitorsControlScreenModel
) {
    FullScreenDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = "Teams",
        maxWidth = 1100.dp
    ) {
        val dataCalc by screenModel.calcTeamsData()
        if (dataCalc is CalcResult.Available) {
            val data = (dataCalc as CalcResult.Available).result
            ClickableTable(
                modifier = Modifier.padding(
                    top = 10.dp,
                    bottom = 20.dp,
                    start = 20.dp,
                    end = 20.dp
                ),
                header = listOf("Name", "Members", "Rounds", "Funfactors", "Total rounds", "Total amount"),
                data = data,
                weights = listOf(3f, 1f, 1f, 1f, 1f, 1f),
                icon = {
                    Icon(
                        modifier = Modifier.size(IconSize.small - 2.dp),
                        imageVector = FontAwesomeIcons.Solid.SlidersH,
                        contentDescription = "Show team details",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                },
                onClick = {
                    onDismissRequest()
                    onShowTeamDetailsRequest(it)
                }
            )
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
private fun ExpandedSingleRunnersTable(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onShowRunnerDetailsRequest: (Runner) -> Unit,
    screenModel: CompetitorsControlScreenModel
) {
    FullScreenDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = "Single runners",
        maxWidth = 800.dp
    ) {
        val dataCalc by screenModel.calcRunnersData()
        if (dataCalc is CalcResult.Available) {
            val data = (dataCalc as CalcResult.Available).result
            ClickableTable(
                modifier = Modifier.padding(
                    top = 10.dp,
                    bottom = 20.dp,
                    start = 20.dp,
                    end = 20.dp
                ),
                header = listOf("ID", "Name", "Rounds", "Total Amount"),
                data = data,
                weights = listOf(1f, 2.8f, 1f, 1f),
                icon = {
                    Icon(
                        modifier = Modifier.size(IconSize.small - 2.dp),
                        imageVector = FontAwesomeIcons.Solid.SlidersH,
                        contentDescription = "Show runner details",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                },
                onClick = {
                    onDismissRequest()
                    onShowRunnerDetailsRequest(it)
                }
            )
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

private sealed class RunnerDetailsStatus {
    data object Closed : RunnerDetailsStatus()
    data class Open(val runner: Runner) : RunnerDetailsStatus()
}

private sealed class TeamDetailsStatus {
    data object Closed : TeamDetailsStatus()
    data class Open(val team: Team) : TeamDetailsStatus()
}
