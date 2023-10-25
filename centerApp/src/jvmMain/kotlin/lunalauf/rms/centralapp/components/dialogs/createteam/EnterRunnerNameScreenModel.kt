package lunalauf.rms.centralapp.components.dialogs.createteam

import LunaLaufLanguage.Team
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.centralapp.components.commons.showSnackbar
import lunalauf.rms.centralapp.utils.InputValidator
import lunalauf.rms.modelapi.AddRunnerToTeamResult
import lunalauf.rms.modelapi.CreateRunnerResult
import lunalauf.rms.modelapi.ModelState

class EnterRunnerNameScreenModel(
    modelState: ModelState.Loaded,
    private val id: ULong,
    private val team: Team,
    private val snackBarHostState: SnackbarHostState
) : ScreenModel, AbstractScreenModel() {
    private val modelAPI = modelState.modelAPI

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
            when (val result = modelAPI.createRunner(id, name)) {
                is CreateRunnerResult.Created -> {
                    when (modelAPI.addRunnerToTeam(team, result.runner)) {
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