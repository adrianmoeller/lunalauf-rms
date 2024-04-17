package lunalauf.rms.centralapp.components.dialogs.logfunfactorresults

import LunaLaufLanguage.Funfactor
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import lunalauf.rms.centralapp.components.commons.ListItemDivider
import lunalauf.rms.modelapi.ModelState

data class ChooseFunfactorScreen(
    private val modelState: ModelState.Loaded,
    private val onDismissRequest: (Navigator) -> Unit,
    private val snackBarHostState: SnackbarHostState
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val minigamesState by modelState.minigames.collectAsState()
        val challengesState by modelState.challenges.collectAsState()
        
        Row(
            modifier = Modifier.padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.padding(10.dp),
                    text = "Minigames:",
                    style = MaterialTheme.typography.labelLarge
                )
                minigamesState.minigames.forEachIndexed { index, minigame ->
                    FunfactorTile(
                        funfactor = minigame,
                        onClick = {
                            navigator.push(
                                AssignResultsScreen(
                                    modelState = modelState,
                                    funfactor = minigame,
                                    onDismissRequest = onDismissRequest,
                                    snackBarHostState = snackBarHostState
                                )
                            )
                        }
                    )
                    if (index < minigamesState.minigames.lastIndex)
                        ListItemDivider(spacing = 10.dp)
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.padding(10.dp),
                    text = "Challenges:",
                    style = MaterialTheme.typography.labelLarge
                )
                challengesState.challenges.forEachIndexed { index, challenge ->
                    FunfactorTile(
                        funfactor = challenge,
                        onClick = {
                            navigator.push(
                                AssignResultsScreen(
                                    modelState = modelState,
                                    funfactor = challenge,
                                    onDismissRequest = onDismissRequest,
                                    snackBarHostState = snackBarHostState
                                )
                            )
                        }
                    )
                    if (index < challengesState.challenges.lastIndex)
                        ListItemDivider(spacing = 10.dp)
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun FunfactorTile(
    funfactor: Funfactor,
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
                .padding(start = 10.dp)
                .weight(1f),
            text = funfactor.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (hovered) {
            Row {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = "Select ${funfactor.name}",
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(10.dp))
            }
        }
    }
}