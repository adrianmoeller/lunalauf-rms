package lunalauf.rms.centralapp.components.dialogs.details.team

import LunaLaufLanguage.ContrType
import LunaLaufLanguage.Team
import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.centralapp.utils.Formats
import lunalauf.rms.centralapp.utils.InputValidator
import lunalauf.rms.modelapi.ModelState
import kotlin.time.Duration.Companion.milliseconds

class TeamDetailsScreenModel(
    modelState: ModelState.Loaded
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

    fun calcStats(team: Team): List<Pair<String, String>> {
        val roundDurations = team.roundDurations()
        val numOfRounds = team.numOfRounds()
        val numOfFunfactorPoints = team.numOfFunfactorPoints()

        return listOf(
            Pair("Rounds", numOfRounds.toString()),
            Pair("Funfactor points", numOfFunfactorPoints.toString()),
            Pair("Total rounds", (numOfRounds + numOfFunfactorPoints).toString()),
            Pair("Total contribution", "${team.totalAmount()} €"),
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

    private fun Team.roundDurations() = rounds
        .filterNotNull()
        .map { it.timestamp.time }
        .sorted()
        .zipWithNext { a, b -> b - a }
}