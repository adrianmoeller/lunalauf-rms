package lunalauf.rms.centralapp.components.dialogs.createteam

import LunaLaufLanguage.Team
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.centralapp.utils.InputValidator
import lunalauf.rms.modelapi.CreateTeamResult
import lunalauf.rms.modelapi.ModelState

class EnterTeamNameScreenModel(
    modelState: ModelState.Loaded
) : ScreenModel, AbstractScreenModel() {
    private val modelAPI = modelState.modelAPI

    var name by mutableStateOf("")
        private set
    var nameValid by mutableStateOf(false)
        private set
    var processing by mutableStateOf(false)
        private set

    fun updateName(name: String) {
        nameValid = name.isNotBlank() && InputValidator.validateName(name)
        this.name = name
    }

    fun createTeam(
        onCreated: (Team) -> Unit
    ) {
        processing = true
        launchInDefaultScope {
            when (val result = modelAPI.createTeam(name)) {
                is CreateTeamResult.Created -> {
                    onCreated(result.team)
                }

                is CreateTeamResult.Exists -> {
                    // TODO
                }

                CreateTeamResult.BlankName -> {
                    // TODO
                }
            }
        }
    }
}