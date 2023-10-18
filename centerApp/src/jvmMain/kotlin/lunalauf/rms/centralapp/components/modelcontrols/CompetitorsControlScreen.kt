package lunalauf.rms.centralapp.components.modelcontrols

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
import lunalauf.rms.centralapp.components.commons.*
import lunalauf.rms.centralapp.components.dialogs.createrunner.CreateRunnerScreen
import lunalauf.rms.centralapp.components.dialogs.createteam.CreateTeamScreen
import lunalauf.rms.modelapi.ModelState
import lunalauf.rms.modelapi.states.RunnersState
import lunalauf.rms.modelapi.states.TeamsState

@Composable
fun CompetitorsControlScreen(
    modifier: Modifier = Modifier,
    modelState: ModelState.Loaded
) {
    val screenModel = remember { CompetitorsControlScreenModel(modelState) }
    val teamsState by modelState.teams.collectAsState()
    val runnersState by modelState.runners.collectAsState()

    var teamsTableOpen by remember { mutableStateOf(false) }
    var runnersTableOpen by remember { mutableStateOf(false) }

    var createTeamOpen by remember { mutableStateOf(false) }
    var createRunnerOpen by remember { mutableStateOf(false) }

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
                        onClick = {
                            // TODO
                        }
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
                        onClick = {
                            // TODO
                        }
                    )
                    if (index < singleRunners.lastIndex)
                        ListItemDivider(spacing = 10.dp)
                }
            }
        }
        OutlinedButton(
            modifier = Modifier.padding(bottom = 20.dp),
            onClick = {
                // TODO
            }
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
            teamsState = teamsState
        )
    }

    if (runnersTableOpen) {
        ExpandedSingleRunnersTable(
            onDismissRequest = { runnersTableOpen = false },
            runnersState = runnersState
        )
    }

    if (createTeamOpen) {
        CreateTeamScreen(
            onDismissRequest = {createTeamOpen = false},
            modelState = modelState
        )
    }

    if (createRunnerOpen) {
        CreateRunnerScreen(
            onDismissRequest = { createRunnerOpen = false },
            modelState = modelState
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
        modifier = modifier
            .fillMaxWidth(),
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
            modifier = Modifier.padding(
                start = 10.dp,
                top = 10.dp,
                bottom = 10.dp
            ),
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
            modifier = Modifier.padding(
                start = 10.dp,
                top = 10.dp,
                bottom = 10.dp
            ),
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
    teamsState: TeamsState
) {
    FullScreenDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = "Teams",
        maxWidth = 1100.dp
    ) {
        val data = teamsState.teams.map {
            val numOfRounds = it.numOfRounds()
            val numOfFunfactorPoints = it.numOfFunfactorPoints()
            listOf(
                it.name ?: "",
                it.members.filterNotNull().size.toString(),
                numOfRounds.toString(),
                numOfFunfactorPoints.toString(),
                (numOfRounds + numOfFunfactorPoints).toString(),
                "${it.totalAmount()} €"
            )
        }
        Table(
            modifier = Modifier.padding(
                top = 10.dp,
                bottom = 20.dp,
                start = 20.dp,
                end = 20.dp
            ),
            header = listOf("Name", "Members", "Rounds", "Funfactors", "Total rounds", "Total amount"),
            data = data,
            weights = listOf(3f, 1f, 1f, 1f, 1f, 1f)
        )
    }
}

@Composable
private fun ExpandedSingleRunnersTable(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    runnersState: RunnersState
) {
    FullScreenDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = "Single runners",
        maxWidth = 800.dp
    ) {
        val data = runnersState.runners
            .filter { it.team == null }
            .map {
                listOf(
                    it.id.toString(),
                    it.name ?: "",
                    it.numOfRounds().toString(),
                    "${it.totalAmount()} €"
                )
            }
        Table(
            modifier = Modifier.padding(
                top = 10.dp,
                bottom = 20.dp,
                start = 20.dp,
                end = 20.dp
            ),
            header = listOf("ID", "Name", "Rounds", "Total Amount"),
            data = data,
            weights = listOf(1f, 2.8f, 1f, 1f)
        )
    }
}
