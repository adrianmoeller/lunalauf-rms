package lunalauf.rms.centralapp.components.modelcontrols

import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.modelapi.ModelState

class RunControlScreenModel(
    modelState: ModelState.Loaded
) : AbstractScreenModel(modelState) {
    fun updateSponsoringPoolAmount(amount: Double) {
        launchInModelScope {
            modelAPI.setSponsoringPoolAmount(amount)
        }
    }

    fun updateSponsoringPoolRounds(rounds: Int) {
        launchInModelScope {
            modelAPI.setSponsoringPoolRounds(rounds)
        }
    }

    fun updateAdditionalContribution(amount: Double) {
        launchInModelScope {
            modelAPI.updateAdditionalContribution { amount }
        }
    }

    fun addToAdditionalContribution(amount: Double) {
        launchInModelScope {
            modelAPI.updateAdditionalContribution { it + amount }
        }
    }
}