package lunalauf.rms.centralapp.components.dialogs.details.team

import LunaLaufLanguage.*
import androidx.compose.runtime.*
import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.centralapp.components.commons.CalcResult
import lunalauf.rms.centralapp.utils.Formats
import lunalauf.rms.centralapp.utils.InputValidator
import lunalauf.rms.modelapi.ModelState
import kotlin.time.Duration.Companion.milliseconds

class TeamDetailsScreenModel(
    private val modelState: ModelState.Loaded
) : AbstractScreenModel(modelState) {
    fun validateName(name: String): String? {
        return if (InputValidator.validateName(name)) name else null
    }

    fun updateName(team: Team, name: String) {
        launchInModelScope {
            modelAPI.updateTeamName(team, name)
        }
    }

    fun updateContribution(team: Team, type: ContrType, amountFixed: Double, amountPerRound: Double) {
        launchInModelScope {
            modelAPI.updateContribution(team, type, amountFixed, amountPerRound)
        }
    }

    @Composable
    fun calcTeamDetails(team: Team): State<CalcResult<TeamDetails>> {
        val teamsState by modelState.teams.collectAsState()
        return produceState<CalcResult<TeamDetails>>(initialValue = CalcResult.Loading(), teamsState) {
            value = CalcResult.Loading()
            launchInModelScope {
                value = CalcResult.Available(
                    TeamDetails(
                        stats = calcStats(team),
                        membersData = modelAPI.members(team)
                            .map {
                                listOf(
                                    it.id.toString(),
                                    it.name ?: "",
                                    it.numOfRounds().toString()
                                )
                            },
                        funfactorResultsData = modelAPI.funfactorResults(team)
                            .map {
                                listOf(
                                    Formats.dayTimeFormat.format(it.timestamp),
                                    it.type.print(),
                                    it.points.toString()
                                )
                            },
                        roundsData = modelAPI.rounds(team)
                            .sortedBy { it.timestamp }
                            .map {
                                val runnerName = it.runner.name
                                listOf(
                                    Formats.dayTimeFormat.format(it.timestamp),
                                    if (runnerName.isNullOrBlank()) it.runner.id.toString() else runnerName,
                                    it.points.toString()
                                )
                            }
                    )
                )
            }
        }
    }

    private suspend fun calcStats(team: Team): List<Pair<String, String>> {
        val roundDurations = modelAPI.roundDurations(team)
        val numOfRounds = modelAPI.numOfRounds(team)
        val numOfFunfactorPoints = modelAPI.numOfFunfactorPoints(team)

        return listOf(
            Pair("Rounds", numOfRounds.toString()),
            Pair("Funfactor points", numOfFunfactorPoints.toString()),
            Pair("Total rounds", (numOfRounds + numOfFunfactorPoints).toString()),
            Pair("Total contribution", "${modelAPI.totalAmount(team)} €"),
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

    private fun Funfactor.print(): String {
        return when (this) {
            is Minigame -> "Minigame: $name"
            is Challenge -> "Challenge: $name"
            else -> ""
        }
    }
}