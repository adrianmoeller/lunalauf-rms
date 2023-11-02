package lunalauf.rms.centralapp.components.dialogs.logfunfactorresults

import LunaLaufLanguage.Funfactor
import LunaLaufLanguage.Team
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import cafe.adriel.voyager.core.model.ScreenModel
import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.modelapi.ModelState

class AssignResultsScreenModel(
    modelState: ModelState.Loaded,
    private val funfactor: Funfactor,
    private val snackBarHostState: SnackbarHostState
) : ScreenModel, AbstractScreenModel(modelState) {
    val funfactorResults = mutableStateMapOf<Team, Int>()
    val assignedTeams = mutableStateListOf<Team>()
    var pointsBeingEdited: Team? by mutableStateOf(null)
        private set
    var currentPoints by mutableStateOf("")
        private set
    var currentPointsValid by mutableStateOf(false)
        private set
    var processing by mutableStateOf(false)
        private set

    private val parsePointsRegex = Regex("[^0-9-]")

    fun updateTeamAssigned(team: Team, assigned: Boolean) {
        if (assigned) {
            if (team !in assignedTeams) assignedTeams.add(team)
            pointsBeingEdited = team
            currentPoints = ""
            currentPointsValid = validatePoints(currentPoints)
        } else {
            funfactorResults.remove(team)
            assignedTeams.remove(team)
            if (pointsBeingEdited == team) {
                pointsBeingEdited = null
                currentPoints = ""
                currentPointsValid = false
            }
        }
    }

    fun updateCurrentPoints(points: String) {
        currentPoints = parsePoints(points)
        currentPointsValid = validatePoints(points)
    }

    fun updatePointsBeingEdited(team: Team?) {
        val constPointsBeingEdited = pointsBeingEdited
        if (constPointsBeingEdited != null) {
            val parsedPoints = currentPoints.toIntOrNull()
            if (currentPointsValid && parsedPoints != null)
                funfactorResults[constPointsBeingEdited] = parsedPoints
            else
                return
        }

        pointsBeingEdited = team
        if (team == null) {
            currentPoints = ""
            currentPointsValid = false
        } else {
            currentPoints = funfactorResults[team]?.toString() ?: ""
            currentPointsValid = validatePoints(currentPoints)
        }
    }

    fun logFunfactorResults(onClose: () -> Unit) {
        processing = true
        launchInModelScope {
            funfactorResults.forEach { (team, points) ->
                modelAPI.logFunfactorResult(team, funfactor, points)
            }
            onClose()
            snackBarHostState.showSnackbar(
                message = "Logged funfactor results",
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
        }
    }

    private fun validatePoints(points: String): Boolean {
        return points.toIntOrNull() != null
    }

    private fun parsePoints(points: String): String {
        return points.trim().replace(parsePointsRegex, "")
    }
}