package lunalauf.rms.centralapp.components.modelcontrols

import LunaLaufLanguage.Runner
import LunaLaufLanguage.Team
import androidx.compose.runtime.*
import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.centralapp.components.commons.CalcResult
import lunalauf.rms.modelapi.ModelState

class CompetitorsControlScreenModel(
    private val modelState: ModelState.Loaded
) : AbstractScreenModel(modelState) {
    @Composable
    fun calcTeamsData(): State<CalcResult<List<Pair<List<String>, Team>>>> {
        val teamsState by modelState.teams.collectAsState()
        return produceState<CalcResult<List<Pair<List<String>, Team>>>>(
            initialValue = CalcResult.Loading(),
            teamsState
        ) {
            launchInModelScope {
                value = CalcResult.Available(
                    teamsState.teams.map {
                        val numOfRounds = modelAPI.numOfRounds(it)
                        val numOfFunfactorPoints = modelAPI.numOfFunfactorPoints(it)
                        Pair(
                            listOf(
                                it.name ?: "",
                                modelAPI.members(it).size.toString(),
                                numOfRounds.toString(),
                                numOfFunfactorPoints.toString(),
                                (numOfRounds + numOfFunfactorPoints).toString(),
                                "${modelAPI.totalAmount(it)} €"
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
        val runnersState by modelState.runners.collectAsState()
        return produceState<CalcResult<List<Pair<List<String>, Runner>>>>(
            initialValue = CalcResult.Loading(),
            runnersState
        ) {
            launchInModelScope {
                value = CalcResult.Available(
                    runnersState.runners
                        .filter { it.team == null }
                        .map {
                            Pair(
                                listOf(
                                    it.id.toString(),
                                    it.name ?: "",
                                    modelAPI.numOfRounds(it).toString(),
                                    "${modelAPI.totalAmount(it)} €"
                                ),
                                it
                            )
                        }
                )
            }
        }
    }
}