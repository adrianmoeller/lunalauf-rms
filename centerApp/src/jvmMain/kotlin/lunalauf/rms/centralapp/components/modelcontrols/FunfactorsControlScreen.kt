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
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ClipboardCheck
import compose.icons.fontawesomeicons.solid.SlidersH
import lunalauf.rms.centralapp.components.commons.IconSize
import lunalauf.rms.centralapp.components.commons.ListItemDivider
import lunalauf.rms.centralapp.components.commons.customScrollbarStyle
import lunalauf.rms.modelapi.ModelState

@Composable
fun FunfactorsControlScreen(
    modifier: Modifier = Modifier,
    modelState: ModelState.Loaded,
    snackBarHostState: SnackbarHostState
) {
    val minigamesState by modelState.minigames.collectAsState()
    val challengesState by modelState.challenges.collectAsState()

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
                onCreate = {
                    // TODO
                }
            ) {
                itemsIndexed(minigamesState.minigames) { index, minigame ->
                    MinigameTile(
                        modifier = Modifier.fillMaxWidth(),
                        minigameName = minigame.name ?: "",
                        onClick = {
                            // TODO
                        }
                    )
                    if (index < minigamesState.minigames.lastIndex)
                        ListItemDivider(spacing = 10.dp)
                }
            }
            FunfactorsControlCard(
                modifier = Modifier.weight(1f),
                title = "Challenges",
                createLabel = "Create challenge",
                onCreate = {
                    // TODO
                }
            ) {
                itemsIndexed(challengesState.challenges) { index, challenge ->
                    ChallengeTile(
                        modifier = Modifier.fillMaxWidth(),
                        challengeName = challenge.name ?: "",
                        onClick = {
                            // TODO
                        }
                    )
                    if (index < challengesState.challenges.lastIndex)
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
                .padding(vertical = 10.dp)
                .padding(start = 10.dp),
            text = minigameName
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
                .padding(vertical = 10.dp)
                .padding(start = 10.dp),
            text = challengeName
        )
        if (hovered) {
            Row {
                Icon(
                    modifier = Modifier.size(IconSize.small - 2.dp),
                    imageVector = FontAwesomeIcons.Solid.SlidersH,
                    contentDescription = "Show challenge details",
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(10.dp))
            }
        }
    }
}