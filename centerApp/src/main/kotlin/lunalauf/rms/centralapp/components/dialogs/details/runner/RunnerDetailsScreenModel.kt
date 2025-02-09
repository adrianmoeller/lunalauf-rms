package lunalauf.rms.centralapp.components.dialogs.details.runner

import androidx.compose.runtime.*
import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.centralapp.components.commons.CalcResult
import lunalauf.rms.centralapp.utils.Formats
import lunalauf.rms.centralapp.utils.InputValidator
import lunalauf.rms.model.api.LogRoundResult
import lunalauf.rms.model.api.ModelState
import lunalauf.rms.model.api.UpdateRunnerIdResult
import lunalauf.rms.model.common.ContributionType
import lunalauf.rms.model.internal.Runner
import lunalauf.rms.model.internal.Team
import kotlin.time.Duration.Companion.milliseconds

class RunnerDetailsScreenModel(
    modelState: ModelState.Loaded
) : AbstractScreenModel(modelState) {

    fun updateChipId(runner: Runner, id: Long) {
        launchInModelScope {
            when (runner.updateChipId(id)) {
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
            runner.updateName(name)
        }
    }

    fun updateTeam(runner: Runner, team: Team?) {
        launchInModelScope {
            if (team == null)
                runner.removeFromTeam()
            else
                team.addRunner(runner)
        }
    }

    fun updateContribution(runner: Runner, type: ContributionType, amountFixed: Double, amountPerRound: Double) {
        launchInModelScope {
            runner.updateContribution(type, amountFixed, amountPerRound)
        }
    }

    fun manuallyLogPoints(runner: Runner, points: Int) {
        launchInModelScope {
            when (runner.logRound(points, true)) {
                LogRoundResult.LastRoundAlreadyLogged -> {}
                is LogRoundResult.Logged -> {}
                LogRoundResult.RunDisabled -> {}
                LogRoundResult.ValidationFailed -> {}
            }
        }
    }

    @Composable
    fun calcRunnerDetails(runner: Runner): State<CalcResult<RunnerDetails>> {
        val rounds by runner.rounds.collectAsState()

        return produceState<CalcResult<RunnerDetails>>(initialValue = CalcResult.Loading(), rounds) {
            value = CalcResult.Loading()
            launchInModelScope {
                value = CalcResult.Available(
                    RunnerDetails(
                        stats = calcStats(runner),
                        roundsData = rounds
                            .map {
                                Pair(
                                    listOf(
                                        Formats.dayTimeFormat.format(it.timestamp.value),
                                        it.points.value.toString()
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
        val roundDurations = runner.getRoundDurations()

        return listOf(
            Pair("Rounds", runner.numOfRounds.value.toString()),
            Pair("Total contribution", "${runner.totalAmount.value} €"),
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