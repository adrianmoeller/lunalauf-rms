package lunalauf.rms.centralapp.components.dialogs.details.challenge

import LunaLaufLanguage.Challenge
import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.centralapp.utils.InputValidator
import lunalauf.rms.modelapi.*
import lunalauf.rms.utilities.network.communication.competitors.CompetitorMessenger

class ChallengeDetailsScreenModel(
    modelState: ModelState.Loaded
) : AbstractScreenModel(modelState) {
    fun validateName(name: String): String? {
        return name.takeIf { InputValidator.validateName(it) && it.isNotBlank() }
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

    fun validateDuration(duration: String): Int? {
        return duration.toIntOrNull()?.takeIf { it >= 0 }
    }

    fun updateDuration(challenge: Challenge, duration: Int) {
        launchInModelScope {
            when (modelAPI.updateChallengeDuration(challenge, duration)) {
                UpdateChallengeDurationResult.NegativeDuration -> {}
                UpdateChallengeDurationResult.Updated -> {}
            }
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

    fun start(challenge: Challenge, competitorMessenger: CompetitorMessenger.Available) {
        launchInModelScope {
            val result = modelAPI.startChallenge(
                challenge = challenge,
                onSendTeamMessage = {
                    try {
                        competitorMessenger.sendToTeams(it)
                        true
                    } catch (e: Throwable) {
                        false
                    }
                },
                onStartAcceptImages = competitorMessenger::startReceiveImagesFromTeams,
                onCompleted = competitorMessenger::stopReceiveImagesFromTeams
            )
            when (result) {
                StartChallengeResult.AlreadyStarted -> {}
                StartChallengeResult.SendMessageFailed -> {}
                StartChallengeResult.Started -> {}
            }
        }
    }

    fun resetState(challenge: Challenge) {
        launchInModelScope {
            when (modelAPI.resetChallengeState(challenge)) {
                ResetChallengeStateResult.NotCompleted -> {}
                ResetChallengeStateResult.Reset -> {}
            }
        }
    }
}