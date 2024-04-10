package lunalauf.rms.centralapp.components.modelcontrols

import LunaLaufLanguage.Challenge
import LunaLaufLanguage.ChallengeState
import LunaLaufLanguage.Minigame
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import compose.icons.EvaIcons
import compose.icons.FontAwesomeIcons
import compose.icons.evaicons.Outline
import compose.icons.evaicons.outline.DoneAll
import compose.icons.evaicons.outline.MoreHorizontal
import compose.icons.evaicons.outline.Power
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ClipboardCheck
import compose.icons.fontawesomeicons.solid.SlidersH
import lunalauf.rms.centralapp.components.commons.IconSize
import lunalauf.rms.centralapp.components.commons.ListItemDivider
import lunalauf.rms.centralapp.components.commons.customScrollbarStyle
import lunalauf.rms.centralapp.components.dialogs.create.challenge.CreateChallengeScreen
import lunalauf.rms.centralapp.components.dialogs.create.minigame.CreateMinigameScreen
import lunalauf.rms.centralapp.components.dialogs.details.challenge.ChallengeDetailsScreen
import lunalauf.rms.centralapp.components.dialogs.details.minigame.MinigameDetailsScreen
import lunalauf.rms.centralapp.components.dialogs.logfunfactorresults.LogFunfactorResultsScreen
import lunalauf.rms.modelapi.ModelState
import lunalauf.rms.utilities.network.bot.BotManager

@Composable
fun FunfactorsControlScreen(
    modifier: Modifier = Modifier,
    botManager: BotManager,
    modelState: ModelState.Loaded,
    snackBarHostState: SnackbarHostState
) {
    val minigamesState by modelState.minigames.collectAsState()
    val challengesState by modelState.challenges.collectAsState()
    val competitorMessenger by botManager.competitorMessenger.collectAsState()

    var createMinigameOpen by remember { mutableStateOf(false) }
    var createChallengeOpen by remember { mutableStateOf(false) }

    var logFunfactorResultsOpen by remember { mutableStateOf(false) }

    var challengeDetailsStatus: ChallengeDetailsStatus by remember { mutableStateOf(ChallengeDetailsStatus.Closed) }
    var minigameDetailsStatus: MinigameDetailsStatus by remember { mutableStateOf(MinigameDetailsStatus.Closed) }

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
            FunfactorsControlCard(
                modifier = Modifier.weight(1f),
                title = "Minigames",
                createLabel = "Create minigame",
                onCreate = { createMinigameOpen = true }
            ) {
                itemsIndexed(minigamesState.minigames) { index, minigame ->
                    MinigameTile(
                        modifier = Modifier.fillMaxWidth(),
                        minigameId = minigame.minigameID,
                        minigameName = minigame.name ?: "",
                        onClick = { minigameDetailsStatus = MinigameDetailsStatus.Open(minigame) }
                    )
                    if (index < minigamesState.minigames.lastIndex)
                        ListItemDivider(spacing = 10.dp)
                }
            }
            FunfactorsControlCard(
                modifier = Modifier.weight(1f),
                title = "Challenges",
                createLabel = "Create challenge",
                onCreate = { createChallengeOpen = true }
            ) {
                itemsIndexed(challengesState.challenges) { index, challenge ->
                    ChallengeTile(
                        modifier = Modifier.fillMaxWidth(),
                        challengeName = challenge.name ?: "",
                        challengeState = challenge.state,
                        onClick = { challengeDetailsStatus = ChallengeDetailsStatus.Open(challenge) }
                    )
                    if (index < challengesState.challenges.lastIndex)
                        ListItemDivider(spacing = 10.dp)
                }
            }
        }
        OutlinedButton(
            modifier = Modifier.padding(bottom = 20.dp),
            onClick = { logFunfactorResultsOpen = true }
        ) {
            Icon(
                modifier = Modifier.size(IconSize.medium),
                imageVector = FontAwesomeIcons.Solid.ClipboardCheck,
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text(
                modifier = Modifier.padding(
                    top = 5.dp,
                    bottom = 7.dp
                ),
                text = "Log funfactor results",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }

    if (createMinigameOpen) {
        CreateMinigameScreen(
            onDismissRequest = { createMinigameOpen = false },
            modelState = modelState,
            snackBarHostState = snackBarHostState
        )
    }

    if (createChallengeOpen) {
        CreateChallengeScreen(
            onDismissRequest = { createChallengeOpen = false },
            modelState = modelState,
            snackBarHostState = snackBarHostState
        )
    }

    if (logFunfactorResultsOpen) {
        LogFunfactorResultsScreen(
            onDismissRequest = { logFunfactorResultsOpen = false },
            modelState = modelState,
            snackBarHostState = snackBarHostState
        )
    }

    if (challengeDetailsStatus is ChallengeDetailsStatus.Open) {
        val challenge = (challengeDetailsStatus as ChallengeDetailsStatus.Open).challenge
        ChallengeDetailsScreen(
            challenge = remember(challengesState) { challenge },
            onDismissRequest = { challengeDetailsStatus = ChallengeDetailsStatus.Closed },
            competitorMessenger = competitorMessenger,
            modelState = modelState,
            snackBarHostState = snackBarHostState
        )
    }

    if (minigameDetailsStatus is MinigameDetailsStatus.Open) {
        val minigame = (minigameDetailsStatus as MinigameDetailsStatus.Open).minigame
        MinigameDetailsScreen(
            minigame = remember(minigamesState) { minigame },
            onDismissRequest = { minigameDetailsStatus = MinigameDetailsStatus.Closed },
            modelState = modelState,
            snackBarHostState = snackBarHostState
        )
    }
}

@Composable
private fun FunfactorsControlCard(
    modifier: Modifier = Modifier,
    title: String,
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
                    modifier = Modifier
                        .heightIn(min = 40.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
private fun MinigameTile(
    modifier: Modifier = Modifier,
    minigameId: Int,
    minigameName: String,
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
            text = "$minigameId: $minigameName"
        )
        if (hovered) {
            Row {
                Icon(
                    modifier = Modifier.size(IconSize.small - 2.dp),
                    imageVector = FontAwesomeIcons.Solid.SlidersH,
                    contentDescription = "Show minigame details",
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(10.dp))
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ChallengeTile(
    modifier: Modifier = Modifier,
    challengeName: String,
    challengeState: ChallengeState,
    onClick: () -> Unit
) {
    var hovered by remember { mutableStateOf(false) }
    val challengeUIState = challengeState.toUIState()

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
            text = challengeName
        )
        Row {
            if (hovered) {
                Icon(
                    modifier = Modifier.size(IconSize.small - 2.dp),
                    imageVector = FontAwesomeIcons.Solid.SlidersH,
                    contentDescription = "Show challenge details",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Icon(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(challengeUIState.color)
                        .padding(4.dp)
                        .size(20.dp),
                    imageVector = challengeUIState.icon,
                    contentDescription = challengeUIState.description
                )
            }
            Spacer(Modifier.width(10.dp))
        }
    }
}

private fun ChallengeState.toUIState(): ChallengeUIState {
    return when (this) {
        ChallengeState.PENDING -> ChallengeUIState.PENDING
        ChallengeState.STARTED -> ChallengeUIState.STARTED
        ChallengeState.COMPLETED -> ChallengeUIState.COMPLETED
    }
}

private enum class ChallengeUIState(
    val icon: ImageVector,
    val color: Color,
    val description: String
) {
    PENDING(
        icon = EvaIcons.Outline.Power,
        color = Color.Transparent,
        description = "Pending"
    ),
    STARTED(
        icon = EvaIcons.Outline.MoreHorizontal,
        color = Color(0xFF285D9D),
        description = "Started"
    ),
    COMPLETED(
        icon = EvaIcons.Outline.DoneAll,
        color = Color(0xFF21B14D),
        description = "Completed"
    )
}

private sealed class ChallengeDetailsStatus {
    data object Closed : ChallengeDetailsStatus()
    data class Open(val challenge: Challenge) : ChallengeDetailsStatus()
}

private sealed class MinigameDetailsStatus {
    data object Closed : MinigameDetailsStatus()
    data class Open(val minigame: Minigame) : MinigameDetailsStatus()
}
