package lunalauf.rms.centralapp.components.dialogs.runnerdetails

import LunaLaufLanguage.ContrType
import LunaLaufLanguage.Runner
import LunaLaufLanguage.Team
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

    fun updateTeam(runner: Runner, team: Team?) {
        launchInModelScope {
            if (team == null)
                modelAPI.removeRunnerFromTeam(runner)
            else
                modelAPI.addRunnerToTeam(team, runner)
        }
    }

    fun updateContribution(runner: Runner, type: ContrType, amountFixed: Double, amountPerRound: Double) {
        launchInModelScope {
            modelAPI.updateContribution(runner, type, amountFixed, amountPerRound)
        }
    }
}