package lunalauf.rms.centralapp.components.modelcontrols

import androidx.compose.runtime.*
import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.centralapp.components.commons.CalcResult
import lunalauf.rms.modelapi.ModelState

class CompetitorsControlScreenModel(
    private val modelState: ModelState.Loaded
) : AbstractScreenModel(modelState) {
    @Composable
    fun calcTeamsData(): State<CalcResult<List<List<String>>>> {
        val teamsState by modelState.teams.collectAsState()
        return produceState<CalcResult<List<List<String>>>>(initialValue = CalcResult.Loading(), teamsState) {
            launchInModelScope {
                value = CalcResult.Available(
                    teamsState.teams.map {
                        val numOfRounds = modelAPI.numOfRounds(it)
                        val numOfFunfactorPoints = modelAPI.numOfFunfactorPoints(it)
                        listOf(
                            it.name ?: "",
                            modelAPI.members(it).size.toString(),
                            numOfRounds.toString(),
                            numOfFunfactorPoints.toString(),
                            (numOfRounds + numOfFunfactorPoints).toString(),
                            "${modelAPI.totalAmount(it)} €"
                        )
                    }
                )
            }
        }
    }

    @Composable
    fun calcRunnersData(): State<CalcResult<List<List<String>>>> {
        val runnersState by modelState.runners.collectAsState()
        return produceState<CalcResult<List<List<String>>>>(initialValue = CalcResult.Loading(), runnersState) {
            launchInModelScope {
                value = CalcResult.Available(
                    runnersState.runners
                        .filter { it.team == null }
                        .map {
                            listOf(
                                it.id.toString(),
                                it.name ?: "",
                                modelAPI.numOfRounds(it).toString(),
                                "${modelAPI.totalAmount(it)} €"
                            )
                        }
                )
            }
        }
    }
}