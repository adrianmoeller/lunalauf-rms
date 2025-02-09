package lunalauf.rms.centralapp.components.modelcontrols

import androidx.compose.runtime.*
import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.centralapp.components.commons.CalcResult
import lunalauf.rms.model.api.ModelState
import lunalauf.rms.model.internal.Runner
import lunalauf.rms.model.internal.Team

class CompetitorsControlScreenModel(
    modelState: ModelState.Loaded
) : AbstractScreenModel(modelState) {
    @Composable
    fun calcTeamsData(): State<CalcResult<List<Pair<List<String>, Team>>>> {
        val teams by event.teams.collectAsState()
        val overallRounds by event.overallRounds.collectAsState()

        return produceState<CalcResult<List<Pair<List<String>, Team>>>>(
            initialValue = CalcResult.Loading(),
            teams,
            overallRounds
        ) {
            launchInModelScope {
                value = CalcResult.Available(
                    teams.map {
                        val numOfRounds = it.numOfRounds.value
                        val numOfFunfactorPoints = it.numOfFunfactorPoints.value
                        Pair(
                            listOf(
                                it.name.value,
                                it.members.value.size.toString(),
                                numOfRounds.toString(),
                                numOfFunfactorPoints.toString(),
                                (numOfRounds + numOfFunfactorPoints).toString(),
                                "${it.totalAmount} €"
                            ),
                            it
                        )
                    }
                )
            }
        }
    }

    @Composable
    fun calcRunnersData(): State<CalcResult<List<Pair<List<String>, Runner>>>> {
        val runners by event.runners.collectAsState()
        val overallRounds by event.overallRounds.collectAsState()

        return produceState<CalcResult<List<Pair<List<String>, Runner>>>>(
            initialValue = CalcResult.Loading(),
            runners,
            overallRounds
        ) {
            launchInModelScope {
                value = CalcResult.Available(
                    runners
                        .filter { it.team.value == null }
                        .map {
                            Pair(
                                listOf(
                                    it.chipId.value.toString(),
                                    it.name.value,
                                    it.numOfRounds.toString(),
                                    "${it.totalAmount} €"
                                ),
                                it
                            )
                        }
                )
            }
        }
    }
}