package lunalauf.rms.centralapp.components.modelcontrols

import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.modelapi.ModelState

class RunControlScreenModel(
    modelState: ModelState.Loaded
) : AbstractScreenModel() {
    private val modelAPI = modelState.modelAPI

    fun updateSponsoringPoolAmount(amount: Double) {
        launchInDefaultScope {
            modelAPI.setSponsoringPoolAmount(amount)
        }
    }

    fun updateSponsoringPoolRounds(rounds: Int) {
        launchInDefaultScope {
            modelAPI.setSponsoringPoolRounds(rounds)
        }
    }

    fun updateAdditionalContribution(amount: Double) {
        launchInDefaultScope {
            modelAPI.updateAdditionalContribution { amount }
        }
    }

    fun addToAdditionalContribution(amount: Double) {
        launchInDefaultScope {
            modelAPI.updateAdditionalContribution { it + amount }
        }
    }
}