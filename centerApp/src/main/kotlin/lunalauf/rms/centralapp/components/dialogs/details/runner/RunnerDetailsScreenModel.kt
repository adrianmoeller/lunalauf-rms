package lunalauf.rms.centralapp.components.dialogs.details.runner

import LunaLaufLanguage.ContrType
import LunaLaufLanguage.Runner
import LunaLaufLanguage.Team
import androidx.compose.runtime.*
import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.centralapp.components.commons.CalcResult
import lunalauf.rms.centralapp.utils.Formats
import lunalauf.rms.centralapp.utils.InputValidator
import lunalauf.rms.modelapi.ModelState
import lunalauf.rms.modelapi.UpdateRunnerIdResult
import kotlin.time.Duration.Companion.milliseconds

class RunnerDetailsScreenModel(
    private val modelState: ModelState.Loaded
) : AbstractScreenModel(modelState) {
    fun updateID(runner: Runner, id: Long) {
        launchInModelScope {
            when (modelAPI.updateRunnerId(runner, id)) {
                is UpdateRunnerIdResult.Exists -> {}
                UpdateRunnerIdResult.Updated -> {}
            }
        }
    }

    fun validateName(name: String): String? {
        return if (InputValidator.validateName(name)) name
        else null
    }

    fun updateName(runner: Runner, name: String) {
        launchInModelScope {
            modelAPI.updateRunnerName(runner, name)
        }
    }

    fun updateTeam(runner: Runner, team: Team?) {
        launchInModelScope {
            if (team == null)
                modelAPI.removeRunnerFromTeam(runner)
            else
                modelAPI.addRunnerToTeam(team, runner)
        }
    }

    fun updateContribution(runner: Runner, type: ContrType, amountFixed: Double, amountPerRound: Double) {
        launchInModelScope {
            modelAPI.updateContribution(runner, type, amountFixed, amountPerRound)
        }
    }

    @Composable
    fun calcRunnerDetails(runner: Runner): State<CalcResult<RunnerDetails>> {
        val runnersState by modelState.runners.collectAsState()
        return produceState<CalcResult<RunnerDetails>>(initialValue = CalcResult.Loading(), runnersState) {
            value = CalcResult.Loading()
            launchInModelScope {
                value = CalcResult.Available(
                    RunnerDetails(
                        stats = calcStats(runner),
                        roundsData = modelAPI.rounds(runner)
                            .map {
                                Pair(
                                    listOf(
                                        Formats.dayTimeFormat.format(it.timestamp),
                                        it.points.toString()
                                    ),
                                    it
                                )
                            }
                    )
                )
            }
        }
    }

    private suspend fun calcStats(runner: Runner): List<Pair<String, String>> {
        val roundDurations = modelAPI.roundDurations(runner)

        return listOf(
            Pair("Rounds", modelAPI.numOfRounds(runner).toString()),
            Pair("Total contribution", "${modelAPI.totalAmount(runner)} €"),
            Pair("Average round duration", average(roundDurations) ?: "-"),
            Pair("Fastest round", min(roundDurations) ?: "-")
        )
    }

    private fun min(roundDurations: List<Long>) = roundDurations
        .minOrNull()?.let { Formats.minutesFormat(it.milliseconds) }

    private fun average(roundDurations: List<Long>): String? {
        val average = roundDurations.average()
        return if (average.isNaN()) null
        else Formats.minutesFormat(average.milliseconds)
    }
}