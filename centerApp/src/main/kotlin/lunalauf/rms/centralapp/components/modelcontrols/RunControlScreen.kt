package lunalauf.rms.centralapp.components.modelcontrols

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import lunalauf.rms.centralapp.components.commons.EditDialog
import lunalauf.rms.centralapp.components.commons.EditableValueTile
import lunalauf.rms.model.api.ModelState
import lunalauf.rms.model.common.RunTimerState
import lunalauf.rms.model.helper.Formats
import kotlin.time.Duration.Companion.seconds

@Composable
fun RunControlScreen(
    modifier: Modifier = Modifier,
    modelState: ModelState.Loaded,
    snackBarHostState: SnackbarHostState
) {
    val event = modelState.event
    val screenModel = remember { RunControlScreenModel(modelState) }

    var runDuration by remember { mutableStateOf(150) } // min
    val runTimer = event.runTimer
    val remainingTime by runTimer.remainingTime.collectAsState()
    val timerState by runTimer.state.collectAsState()

    val sponsorPoolAmount by event.sponsorPoolAmount.collectAsState()
    val sponsorPoolRounds by event.sponsorPoolRounds.collectAsState()
    val additionalContribution by event.additionalContribution.collectAsState()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        RunClock(
            modifier = Modifier.padding(
                top = 20.dp,
                bottom = 10.dp
            ),
            remainingTime = Formats.clockFormat(remainingTime.seconds),
            state = timerState,
            onStartStopClick = {
                when (timerState) {
                    RunTimerState.RUNNING -> runTimer.pause()
                    RunTimerState.PAUSED -> runTimer.resume()
                    RunTimerState.EXPIRED -> runTimer.start(runDuration * 60L)
                }
            },
            onResetClick = { runTimer.reset() },
            runDuration = runDuration,
            onRunDurationChange = { runDuration = it }
        )
        Row(
            modifier = Modifier
                .padding(20.dp)
                .sizeIn(
                    maxWidth = 800.dp,
                    maxHeight = 350.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            RunControlCard(
                modifier = Modifier.weight(1f),
                title = "Sponsoring pool"
            ) {
                EditableValueTile(
                    name = "Amount",
                    value = sponsorPoolAmount,
                    onValueChange = screenModel::updateSponsoringPoolAmount,
                    parser = screenModel::validateSponsoringPoolAmount,
                    default = 0.0,
                    unit = "€",
                    editTitle = "Update pool amount"
                )
                EditableValueTile(
                    name = "Rounds to be reached",
                    value = sponsorPoolRounds,
                    onValueChange = screenModel::updateSponsoringPoolRounds,
                    parser = screenModel::validateSponsoringPoolRounds,
                    default = 0,
                    editTitle = "Update pool rounds"
                )
            }
            RunControlCard(
                modifier = Modifier.weight(1f),
                title = "Additional donations"
            ) {
                EditableValueTile(
                    name = "Amount",
                    value = additionalContribution,
                    onValueChange = screenModel::updateAdditionalContribution,
                    parser = screenModel::validateAdditionalContribution,
                    default = 0.0,
                    unit = "€",
                    editTitle = "Update additional donations"
                )
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    var addToAddContrDialogOpen by remember { mutableStateOf(false) }
                    var addToAddContrValue by remember {
                        mutableStateOf(TextFieldValue(additionalContribution.toString()))
                    }
                    OutlinedButton(
                        modifier = Modifier.padding(bottom = 5.dp),
                        onClick = {
                            addToAddContrValue = TextFieldValue("")
                            addToAddContrDialogOpen = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(2.dp))
                        Text("Add to amount")
                    }
                    EditDialog(
                        editTitle = "Add to amount",
                        editDialogOpen = addToAddContrDialogOpen,
                        onClose = { addToAddContrDialogOpen = false },
                        onValueChange = screenModel::addToAdditionalContribution,
                        valueName = "Amount to add",
                        newValue = addToAddContrValue,
                        onNewValueChange = { addToAddContrValue = it },
                        parser = screenModel::validateAdditionalContribution,
                        default = 0.0,
                        unit = "€"
                    )
                }
            }
        }
    }
}

@Composable
private fun RunControlCard(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 15.dp,
                vertical = 10.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp),
                content = content
            )
        }
    }
}
