package lunalauf.rms.centralapp.components.modelcontrols

import LunaLaufLanguage.Runner
import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.centralapp.utils.InputValidator
import lunalauf.rms.modelapi.ModelState
import lunalauf.rms.modelapi.UpdateRunnerIdResult

class RunnerDetailsScreenModel(
    modelState: ModelState.Loaded
) : AbstractScreenModel() {
    private val modelAPI = modelState.modelAPI

    fun updateID(runner: Runner, id: ULong) {
        launchInModelScope {
            when (val result = modelAPI.updateRunnerId(runner, id)) {
                is UpdateRunnerIdResult.Exists -> {
                    // TODO
                }
                UpdateRunnerIdResult.Updated -> {}
            }
        }
    }

    fun validateName(name: String): String? {
        return if (InputValidator.validateName(name)) name else null
    }

    fun updateName(runner: Runner, name: String) {
        launchInModelScope {
            modelAPI.updateRunnerName(runner, name)
        }
    }
}