package lunalauf.rms.centralapp.components.dialogs.create.team

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
import lunalauf.rms.modelapi.CreateTeamResult
import lunalauf.rms.modelapi.ModelState

class EnterTeamNameScreenModel(
    private val modelState: ModelState.Loaded,
    private val snackBarHostState: SnackbarHostState
) : ScreenModel, AbstractScreenModel(modelState) {
    var name by mutableStateOf("")
        private set
    var nameValid by mutableStateOf(false)
        private set
    var nameDuplicate by mutableStateOf(false)
        private set
    var processing by mutableStateOf(false)
        private set

    fun updateName(name: String) {
        nameDuplicate = modelState.teams.value.names.contains(name)
        nameValid = name.isNotBlank() && InputValidator.validateName(name) && !nameDuplicate
        this.name = name
    }

    fun createTeam(
        onCreated: (Team) -> Unit,
        onClose: () -> Unit
    ) {
        processing = true
        launchInModelScope {
            when (val result = modelAPI.createTeam(name)) {
                is CreateTeamResult.Created -> {
                    onCreated(result.team)
                }

                is CreateTeamResult.Exists -> {
                    onClose()
                    launchInDefaultScope {
                        snackBarHostState.showSnackbar(
                            message = "A team with name '${result.team.name}' already exists",
                            withDismissAction = true,
                            duration = SnackbarDuration.Long,
                            isError = true
                        )
                    }
                }

                CreateTeamResult.BlankName -> {
                    onClose()
                    launchInDefaultScope {
                        snackBarHostState.showSnackbar(
                            message = "Please provide a non-blank team name",
                            withDismissAction = true,
                            duration = SnackbarDuration.Long,
                            isError = true
                        )
                    }
                }
            }
        }
    }
}