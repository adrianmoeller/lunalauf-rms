package lunalauf.rms.centralapp.components.dialogs.details.challenge

import LunaLaufLanguage.Challenge
import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.centralapp.utils.InputValidator
import lunalauf.rms.modelapi.ModelState
import lunalauf.rms.modelapi.UpdateChallengeNameResult

class ChallengeDetailsScreenModel(
    modelState: ModelState.Loaded
) : AbstractScreenModel(modelState) {
    fun validateName(name: String): String? {
        return if (InputValidator.validateName(name) && name.isNotBlank()) name
        else null
    }

    fun updateName(challenge: Challenge, name: String) {
        launchInModelScope {
            when (modelAPI.updateChallengeName(challenge, name)) {
                UpdateChallengeNameResult.BlankName -> {}
                UpdateChallengeNameResult.Updated -> {}
            }
        }
    }

    fun validateDescription(description: String): String {
        return description
    }

    fun updateDescription(challenge: Challenge, description: String) {
        launchInModelScope {
            modelAPI.updateChallengeDescription(challenge, description)
        }
    }

    fun updateExpires(challenge: Challenge, expires: Boolean) {
        launchInModelScope {
            modelAPI.updateChallengeExpires(challenge, expires)
        }
    }

    fun validateDuration(duration: String): UInt? {
        return duration.toUIntOrNull()
    }

    fun updateDuration(challenge: Challenge, duration: UInt) {
        launchInModelScope {
            modelAPI.updateChallengeDuration(challenge, duration)
        }
    }

    fun updateExpireMessage(challenge: Challenge, expireMessage: String) {
        launchInModelScope {
            modelAPI.updateChallengeExpireMessage(challenge, expireMessage)
        }
    }

    fun validateExpireMessage(expireMessage: String): String {
        return expireMessage
    }

    fun updateReceiveImage(challenge: Challenge, receiveImage: Boolean) {
        launchInModelScope {
            modelAPI.updateChallengeReceiveImage(challenge, receiveImage)
        }
    }
}