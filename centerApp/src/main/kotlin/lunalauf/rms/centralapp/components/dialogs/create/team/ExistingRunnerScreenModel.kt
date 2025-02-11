package lunalauf.rms.centralapp.components.dialogs.create.team

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.centralapp.components.commons.showSnackbar
import lunalauf.rms.model.api.AddRunnerToTeamResult
import lunalauf.rms.model.api.ModelState
import lunalauf.rms.model.internal.Runner
import lunalauf.rms.model.internal.Team

class ExistingRunnerScreenModel(
    modelState: ModelState.Loaded,
    private val team: Team,
    private val runner: Runner,
    private val snackBarHostState: SnackbarHostState
) : ScreenModel, AbstractScreenModel(modelState) {
    private val runnersTeam = runner.team.value

    var displayMessage by mutableStateOf(
        if (runnersTeam == null) "Existing single runner:"
        else "Runner from another team '${runnersTeam.name.value}':"
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
            when (team.addRunner(runner)) {
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