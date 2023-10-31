package lunalauf.rms.centralapp.components.dialogs.create.team

import LunaLaufLanguage.Runner
import LunaLaufLanguage.Team
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.centralapp.components.commons.showSnackbar
import lunalauf.rms.modelapi.AddRunnerToTeamResult
import lunalauf.rms.modelapi.ModelState

class ExistingRunnerScreenModel(
    modelState: ModelState.Loaded,
    private val team: Team,
    private val runner: Runner,
    private val snackBarHostState: SnackbarHostState
) : ScreenModel, AbstractScreenModel(modelState) {
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
                    launchInDefaultScope {
                        snackBarHostState.showSnackbar(
                            message = "This runner is already a team member",
                            withDismissAction = true,
                            duration = SnackbarDuration.Long,
                            isError = true
                        )
                    }
                    onClose()
                }
            }
        }
    }
}