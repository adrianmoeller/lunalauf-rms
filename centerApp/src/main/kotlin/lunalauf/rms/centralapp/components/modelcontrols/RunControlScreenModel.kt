package lunalauf.rms.centralapp.components.modelcontrols

import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.modelapi.ModelState

class RunControlScreenModel(
    modelState: ModelState.Loaded
) : AbstractScreenModel(modelState) {
    fun validateSponsoringPoolAmount(amount: String): Double? {
        return amount.trim().toDoubleOrNull()?.takeIf { it >= 0 }
    }

    fun updateSponsoringPoolAmount(amount: Double) {
        launchInModelScope {
            modelAPI.setSponsoringPoolAmount(amount)
        }
    }

    fun validateSponsoringPoolRounds(rounds: String): Int? {
        return rounds.trim().toIntOrNull()?.takeIf { it >= 0 }
    }

    fun updateSponsoringPoolRounds(rounds: Int) {
        launchInModelScope {
            modelAPI.setSponsoringPoolRounds(rounds)
        }
    }

    fun validateAdditionalContribution(amount: String): Double? {
        return amount.trim().toDoubleOrNull()?.takeIf { it >= 0 }
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