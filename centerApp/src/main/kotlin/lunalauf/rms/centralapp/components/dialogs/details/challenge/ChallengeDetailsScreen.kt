package lunalauf.rms.centralapp.components.dialogs.details.challenge

import LunaLaufLanguage.Challenge
import LunaLaufLanguage.ChallengeState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import lunalauf.rms.centralapp.components.commons.*
import lunalauf.rms.modelapi.ModelState
import lunalauf.rms.utilities.network.communication.competitors.CompetitorMessenger

@Composable
fun ChallengeDetailsScreen(
    modifier: Modifier = Modifier,
    challenge: Challenge,
    onDismissRequest: () -> Unit,
    competitorMessenger: CompetitorMessenger,
    modelState: ModelState.Loaded,
    snackBarHostState: SnackbarHostState
) {
    val screenModel = remember { ChallengeDetailsScreenModel(modelState) }

    FullScreenDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = "Challenge: ${challenge.name}",
        maxWidth = 600.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 15.dp,
                        vertical = 10.dp
                    )
                ) {
                    Box {
                        val listState = rememberLazyListState()
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                            state = listState
                        ) {
                            item {
                                EditableValueTile(
                                    name = "Name",
                                    value = challenge.name,
                                    onValueChange = { screenModel.updateName(challenge, it) },
                                    parser = screenModel::validateName,
                                    default = "",
                                    editTitle = "Update name"
                                )
                            }
                            item {
                                EditableLongValueTile(
                                    name = "Description",
                                    value = challenge.description,
                                    onValueChange = { screenModel.updateDescription(challenge, it) },
                                    parser = screenModel::validateDescription,
                                    default = "",
                                    editTitle = "Update description"
                                )
                            }
                            item {
                                OptionTile(
                                    text = "Expires",
                                    checked = challenge.isExpires,
                                    onCheckedChange = { screenModel.updateExpires(challenge, it) }
                                )
                            }
                            if (challenge.isExpires) {
                                item {
                                    EditableValueTile(
                                        name = "Duration",
                                        value = challenge.duration,
                                        onValueChange = { screenModel.updateDuration(challenge, it) },
                                        parser = screenModel::validateDuration,
                                        default = 0,
                                        unit = "min",
                                        editTitle = "Update duration"
                                    )
                                }
                                item {
                                    EditableLongValueTile(
                                        name = "Expire message",
                                        value = challenge.expireMsg,
                                        onValueChange = { screenModel.updateExpireMessage(challenge, it) },
                                        parser = screenModel::validateExpireMessage,
                                        default = "",
                                        editTitle = "Update expire message"
                                    )
                                }
                                item {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = challenge.isReceiveImages,
                                            onCheckedChange = { screenModel.updateReceiveImage(challenge, it) }
                                        )
                                        Text("Receive image")
                                    }
                                }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DeleteElementDialog(
                    element = challenge,
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
                        Text("Delete challenge")
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    IconButton(
                        onClick = { screenModel.resetState(challenge) },
                        enabled = challenge.state == ChallengeState.COMPLETED
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Reset challenge state"
                        )
                    }
                    if (competitorMessenger is CompetitorMessenger.Available) {
                        OutlinedButton(
                            onClick = { screenModel.start(challenge, competitorMessenger) },
                            enabled = challenge.state == ChallengeState.PENDING
                        ) {
                            when (challenge.state) {
                                ChallengeState.PENDING -> {
                                    Icon(
                                        imageVector = Icons.Rounded.PlayArrow,
                                        contentDescription = null
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Start")
                                }

                                ChallengeState.STARTED -> Text("Started")
                                ChallengeState.COMPLETED, null -> Text("Completed")
                            }
                        }
                    } else {
                        Text(
                            text = "Competitor messenger not available",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}