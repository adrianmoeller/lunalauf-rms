package lunalauf.rms.centralapp.components.dialogs.create.team

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.centralapp.components.commons.showSnackbar
import lunalauf.rms.centralapp.utils.InputValidator
import lunalauf.rms.model.api.AddRunnerToTeamResult
import lunalauf.rms.model.api.CreateRunnerResult
import lunalauf.rms.model.api.ModelState
import lunalauf.rms.model.internal.Team

class EnterRunnerNameScreenModel(
    modelState: ModelState.Loaded,
    private val id: Long,
    private val team: Team,
    private val snackBarHostState: SnackbarHostState
) : ScreenModel, AbstractScreenModel(modelState) {
    var name by mutableStateOf("")
        private set
    var nameValid by mutableStateOf(true)
        private set
    var processing by mutableStateOf(false)
        private set

    fun updateName(name: String) {
        nameValid = InputValidator.validateName(name)
        this.name = name
    }

    fun createRunnerAndAddToTeam(
        onClose: () -> Unit,
        onBack: () -> Unit
    ) {
        processing = true
        launchInModelScope {
            when (val result = event.createRunner(id, name)) {
                is CreateRunnerResult.Created -> {
                    when (team.addRunner(result.runner)) {
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

                is CreateRunnerResult.Exists -> {
                    launchInDefaultScope {
                        snackBarHostState.showSnackbar(
                            message = "A runner with this ID already exists",
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