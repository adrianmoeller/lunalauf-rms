package lunalauf.rms.centralapp.components.modelcontrols

import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.model.api.ModelState

class RunControlScreenModel(
    modelState: ModelState.Loaded
) : AbstractScreenModel(modelState) {
    fun validateSponsoringPoolAmount(amount: String): Double? {
        return amount.trim().toDoubleOrNull()?.takeIf { it >= 0 }
    }

    fun updateSponsoringPoolAmount(amount: Double) {
        launchInModelScope {
            event.setSponsoringPoolAmount(amount)
        }
    }

    fun validateSponsoringPoolRounds(rounds: String): Int? {
        return rounds.trim().toIntOrNull()?.takeIf { it >= 0 }
    }

    fun updateSponsoringPoolRounds(rounds: Int) {
        launchInModelScope {
            event.setSponsoringPoolRounds(rounds)
        }
    }

    fun validateAdditionalContribution(amount: String): Double? {
        return amount.trim().toDoubleOrNull()?.takeIf { it >= 0 }
    }

    fun updateAdditionalContribution(amount: Double) {
        launchInModelScope {
            event.updateAdditionalContribution { amount }
        }
    }

    fun addToAdditionalContribution(amount: Double) {
        launchInModelScope {
            event.updateAdditionalContribution { it + amount }
        }
    }
}