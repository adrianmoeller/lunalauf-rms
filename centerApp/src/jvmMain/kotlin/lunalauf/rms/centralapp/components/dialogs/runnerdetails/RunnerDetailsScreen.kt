package lunalauf.rms.centralapp.components.dialogs.runnerdetails

import LunaLaufLanguage.Runner
import androidx.compose.foundation.layout.*
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import lunalauf.rms.centralapp.components.commons.EditableValueTile
import lunalauf.rms.centralapp.components.commons.FullScreenDialog
import lunalauf.rms.modelapi.ModelState

@Composable
fun RunnerDetailsScreen(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    modelState: ModelState.Loaded,
    runner: Runner
) {
    val screenModel = remember { RunnerDetailsScreenModel(modelState) }

    FullScreenDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = "Runner: ${if (runner.name.isNullOrBlank()) runner.id else runner.name}",
        maxWidth = 800.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            OutlinedCard(
                modifier = Modifier
                    .padding(20.dp)
                    .widthIn(max = 450.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 15.dp,
                        vertical = 10.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    EditableIDTile(
                        value = runner.id.toULong(),
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
        }
    }
}
