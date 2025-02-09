package lunalauf.rms.centralapp.components.dialogs.details.team

import androidx.compose.runtime.*
import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.centralapp.components.commons.CalcResult
import lunalauf.rms.centralapp.utils.Formats
import lunalauf.rms.centralapp.utils.InputValidator
import lunalauf.rms.model.api.ModelState
import lunalauf.rms.model.common.ContributionType
import lunalauf.rms.model.internal.Team
import kotlin.time.Duration.Companion.milliseconds

class TeamDetailsScreenModel(
    private val modelState: ModelState.Loaded
) : AbstractScreenModel(modelState) {
    fun validateName(name: String): String? {
        return if (InputValidator.validateName(name)) name else null
    }

    fun updateName(team: Team, name: String) {
        launchInModelScope {
            team.updateName(name)
        }
    }

    fun updateContribution(team: Team, type: ContributionType, amountFixed: Double, amountPerRound: Double) {
        launchInModelScope {
            team.updateContribution(type, amountFixed, amountPerRound)
        }
    }

    @Composable
    fun calcTeamDetails(team: Team): State<CalcResult<TeamDetails>> {
        val members by team.members.collectAsState()
        val rounds by team.rounds.collectAsState()
        val funfactorResults by team.funfactorResults.collectAsState()

        return produceState<CalcResult<TeamDetails>>(
            initialValue = CalcResult.Loading(),
            members,
            rounds,
            funfactorResults
        ) {
            value = CalcResult.Loading()
            launchInModelScope {
                value = CalcResult.Available(
                    TeamDetails(
                        stats = calcStats(team),
                        membersData = members
                            .map {
                                Pair(
                                    listOf(
                                        it.chipId.value.toString(),
                                        it.name.value,
                                        it.numOfRounds.value.toString()
                                    ),
                                    it
                                )
                            },
                        funfactorResultsData = funfactorResults
                            .map {
                                Pair(
                                    listOf(
                                        Formats.dayTimeFormat.format(it.timestamp.value),
                                        it.type.value.print(),
                                        it.points.value.toString()
                                    ),
                                    it
                                )
                            },
                        roundsData = rounds
                            .sortedBy { it.timestamp.value }
                            .map {
                                val runnerName = it.runner.value.name.value
                                Pair(
                                    listOf(
                                        Formats.dayTimeFormat.format(it.timestamp),
                                        runnerName.ifBlank { it.runner.value.chipId.value.toString() },
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

    private suspend fun calcStats(team: Team): List<Pair<String, String>> {
        val roundDurations = team.getRoundDurations()
        val numOfRounds = team.numOfRounds.value
        val numOfFunfactorPoints = team.numOfFunfactorPoints.value

        return listOf(
            Pair("Rounds", numOfRounds.toString()),
            Pair("Funfactor points", numOfFunfactorPoints.toString()),
            Pair("Total rounds", (numOfRounds + numOfFunfactorPoints).toString()),
            Pair("Total contribution", "${team.totalAmount.value} €"),
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