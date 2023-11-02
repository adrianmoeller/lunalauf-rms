package lunalauf.rms.centralapp.components.dialogs.logfunfactorresults

import LunaLaufLanguage.Funfactor
import LunaLaufLanguage.Team
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import lunalauf.rms.centralapp.components.commons.ListItemDivider
import lunalauf.rms.centralapp.components.commons.customScrollbarStyle
import lunalauf.rms.centralapp.components.commons.tryRequestFocusWithScope
import lunalauf.rms.modelapi.ModelState

data class AssignResultsScreen(
    private val modelState: ModelState.Loaded,
    private val funfactor: Funfactor,
    private val onDismissRequest: (Navigator) -> Unit,
    private val snackBarHostState: SnackbarHostState
) : Screen {
    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel {
            AssignResultsScreenModel(
                modelState = modelState,
                funfactor = funfactor,
                snackBarHostState = snackBarHostState
            )
        }
        val teamsState by modelState.teams.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        Box(
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .alpha(if (screenModel.processing) 0f else 1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Funfactor: ${funfactor.name}",
                    style = MaterialTheme.typography.titleMedium
                )
                Box(
                    modifier = Modifier.weight(1f, false)
                ) {
                    val density = LocalDensity.current
                    val listState = rememberLazyListState()
                    var listHeight by remember { mutableStateOf(0.dp) }
                    LazyColumn(
                        modifier = Modifier.onGloballyPositioned {
                            listHeight = with(density) {
                                it.size.height.toDp()
                            }
                        },
                        verticalArrangement = Arrangement.spacedBy(15.dp),
                        state = listState
                    ) {
                        item {
                            Card {
                                Column {
                                    val remainingTeams = teamsState.teams.filterNot { it in screenModel.assignedTeams }
                                    AllTeamsTile(
                                        onAssignAll = screenModel::assignRemainingTeams,
                                        validatePoints = screenModel::validatePoints,
                                        parsePoints = screenModel::parsePoints,
                                        enabled = remainingTeams.isNotEmpty()
                                    )
                                    remainingTeams.forEach { team ->
                                        ListItemDivider(spacing = 10.dp)
                                        RemainingTeamTile(
                                            team = team,
                                            onClick = { screenModel.updateTeamAssigned(team, true) }
                                        )
                                    }
                                }
                            }
                        }
                        item {
                            Column {
                                screenModel.assignedTeams.forEachIndexed { index, team ->
                                    if (team == screenModel.pointsBeingEdited) {
                                        AssignedEditingTeamTile(
                                            team = team,
                                            points = screenModel.currentPoints,
                                            pointsValid = screenModel.currentPointsValid,
                                            onPointsChange = screenModel::updateCurrentPoints,
                                            onEditDone = { screenModel.updatePointsBeingEdited(null) }
                                        )
                                    } else {
                                        AssignedTeamTile(
                                            team = team,
                                            points = screenModel.funfactorResults[team] ?: 0,
                                            onEditClick = { screenModel.updatePointsBeingEdited(team) },
                                            onUnassignClick = { screenModel.updateTeamAssigned(team, false) }
                                        )
                                    }
                                    if (index < screenModel.assignedTeams.lastIndex)
                                        ListItemDivider(spacing = 10.dp)
                                }
                            }
                        }
                    }
                    VerticalScrollbar(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .height(listHeight),
                        adapter = rememberScrollbarAdapter(listState),
                        style = customScrollbarStyle
                    )
                }
                Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = {
                            navigator.pop()
                        },
                        enabled = !screenModel.processing
                    ) {
                        Text("Back")
                    }
                    FilledTonalButton(
                        onClick = {
                            screenModel.logFunfactorResults(
                                onClose = { onDismissRequest(navigator) }
                            )
                        },
                        enabled = screenModel.assignedTeams.isNotEmpty() && !screenModel.processing
                    ) {
                        Text("Log results")
                    }
                }
            }
            if (screenModel.processing)
                CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun AllTeamsTile(
    onAssignAll: (Int) -> Unit,
    validatePoints: (String) -> Boolean,
    parsePoints: (String) -> String,
    enabled: Boolean
) {
    var hovered by remember { mutableStateOf(false) }
    var setPointsOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onPointerEvent(PointerEventType.Enter) { hovered = true }
            .onPointerEvent(PointerEventType.Exit) { hovered = false }
            .clip(MaterialTheme.shapes.medium)
            .clickable(enabled = enabled) { setPointsOpen = true },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier
                .padding(vertical = 10.dp)
                .padding(start = 10.dp),
            text = "All",
            fontStyle = FontStyle.Italic
        )
        if (hovered && enabled) {
            Row {
                Icon(
                    imageVector = Icons.Rounded.ArrowForward,
                    contentDescription = "Assign points",
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(10.dp))
            }
        }
    }

    if (setPointsOpen) {
        var points by remember { mutableStateOf("") }
        var pointsValid by remember { mutableStateOf(false) }
        val focusRequester = remember { FocusRequester() }

        AlertDialog(
            title = { Text("Set points for all teams") },
            text = {
                OutlinedTextField(
                    modifier = Modifier
                        .onPreviewKeyEvent {
                            if (it.key == Key.Enter) {
                                if (it.type == KeyEventType.KeyUp && pointsValid) {
                                    onAssignAll(points.toIntOrNull() ?: 0)
                                    setPointsOpen = false
                                }
                                return@onPreviewKeyEvent true
                            }
                            return@onPreviewKeyEvent false
                        }
                        .focusRequester(focusRequester),
                    label = { Text("Points") },
                    value = points,
                    onValueChange = {
                        pointsValid = validatePoints(it)
                        points = parsePoints(it)
                    },
                    isError = !pointsValid
                )

            },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        onAssignAll(points.toIntOrNull() ?: 0)
                        setPointsOpen = false
                    },
                    enabled = pointsValid
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { setPointsOpen = false }
                ) {
                    Text("Cancel")
                }
            },
            onDismissRequest = { setPointsOpen = false }
        )

        focusRequester.requestFocus()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun RemainingTeamTile(
    team: Team,
    onClick: () -> Unit
) {
    var hovered by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onPointerEvent(PointerEventType.Enter) { hovered = true }
            .onPointerEvent(PointerEventType.Exit) { hovered = false }
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier
                .padding(vertical = 10.dp)
                .padding(start = 10.dp),
            text = team.name
        )
        if (hovered) {
            Row {
                Icon(
                    imageVector = Icons.Rounded.ArrowForward,
                    contentDescription = "Assign points",
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(10.dp))
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun AssignedTeamTile(
    team: Team,
    points: Int,
    onEditClick: () -> Unit,
    onUnassignClick: () -> Unit
) {
    var hovered by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .onPointerEvent(PointerEventType.Enter) { hovered = true }
            .onPointerEvent(PointerEventType.Exit) { hovered = false }
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onEditClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 10.dp,
                    vertical = 5.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${team.name}:")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (hovered)
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit points"
                    )
                Text(
                    text = "$points points",
                    fontWeight = FontWeight.Bold
                )
                FilledTonalIconButton(
                    onClick = onUnassignClick
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Unassign points"
                    )
                }
            }
        }
    }
}

@Composable
private fun AssignedEditingTeamTile(
    team: Team,
    points: String,
    pointsValid: Boolean,
    onPointsChange: (String) -> Unit,
    onEditDone: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("${team.name}:")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .widthIn(max = 100.dp)
                    .onPreviewKeyEvent {
                        if (it.key == Key.Enter) {
                            if (it.type == KeyEventType.KeyUp && pointsValid) {
                                onEditDone()
                            }
                            return@onPreviewKeyEvent true
                        }
                        return@onPreviewKeyEvent false
                    }
                    .focusRequester(focusRequester),
                label = { Text("Points") },
                value = points,
                onValueChange = onPointsChange,
                isError = !pointsValid
            )
            FilledTonalIconButton(
                onClick = onEditDone,
                enabled = pointsValid
            ) {
                Icon(
                    imageVector = Icons.Rounded.Done,
                    contentDescription = "Done editing points"
                )
            }
        }
    }

    focusRequester.tryRequestFocusWithScope(coroutineScope)
}