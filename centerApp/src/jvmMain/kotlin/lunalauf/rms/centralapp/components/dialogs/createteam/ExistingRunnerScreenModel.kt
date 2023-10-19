package lunalauf.rms.centralapp.components.dialogs.createteam

import LunaLaufLanguage.Runner
import LunaLaufLanguage.Team
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.modelapi.AddRunnerToTeamResult
import lunalauf.rms.modelapi.ModelState

class ExistingRunnerScreenModel(
    modelState: ModelState.Loaded,
    private val team: Team,
    private val runner: Runner
) : ScreenModel, AbstractScreenModel() {
    private val modelAPI = modelState.modelAPI

    var displayMessage by mutableStateOf(
        if (runner.team == null) "Existing single runner:"
        else "Runner from another team '${runner.team.name}':"
    )
        private set
    var processing by mutableStateOf(false)
        private set

    fun addRunnerToTeam(
        onBack: () -> Unit,
        onClose: () -> Unit
    ) {
        processing = true
        launchInModelScope {
            when (modelAPI.addRunnerToTeam(team, runner)) {
                AddRunnerToTeamResult.Added -> {
                    onBack()
                }
                AddRunnerToTeamResult.AlreadyMember -> {
                    // TODO

                    onClose()
                }
            }
        }
    }
}