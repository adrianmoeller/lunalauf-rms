package lunalauf.rms.centralapp.components.dialogs.details.challenge

import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.centralapp.utils.InputValidator
import lunalauf.rms.model.api.*
import lunalauf.rms.model.internal.Challenge
import lunalauf.rms.utilities.network.communication.competitors.CompetitorMessenger

class ChallengeDetailsScreenModel(
    modelState: ModelState.Loaded
) : AbstractScreenModel(modelState) {
    fun validateName(name: String): String? {
        return name.takeIf { InputValidator.validateName(it) && it.isNotBlank() }
    }

    fun updateName(challenge: Challenge, name: String) {
        launchInModelScope {
            when (challenge.updateName(name)) {
                UpdateFunfactorNameResult.BlankName -> {}
                UpdateFunfactorNameResult.Updated -> {}
            }
        }
    }

    fun validateDescription(description: String): String {
        return description
    }

    fun updateDescription(challenge: Challenge, description: String) {
        launchInModelScope {
            challenge.updateDescription(description)
        }
    }

    fun updateExpires(challenge: Challenge, expires: Boolean) {
        launchInModelScope {
            challenge.updateExpires(expires)
        }
    }

    fun validateDuration(duration: String): Int? {
        return duration.toIntOrNull()?.takeIf { it >= 0 }
    }

    fun updateDuration(challenge: Challenge, duration: Int) {
        launchInModelScope {
            when (challenge.updateDuration(duration)) {
                UpdateChallengeDurationResult.NegativeDuration -> {}
                UpdateChallengeDurationResult.Updated -> {}
            }
        }
    }

    fun updateExpireMessage(challenge: Challenge, expireMessage: String) {
        launchInModelScope {
            challenge.updateExpireMessage(expireMessage)
        }
    }

    fun validateExpireMessage(expireMessage: String): String {
        return expireMessage
    }

    fun updateReceiveImage(challenge: Challenge, receiveImage: Boolean) {
        launchInModelScope {
            challenge.updateReceiveImage(receiveImage)
        }
    }

    fun start(challenge: Challenge, competitorMessenger: CompetitorMessenger.Available) {
        launchInModelScope {
            val result = challenge.start(
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
            when (challenge.resetState()) {
                ResetChallengeStateResult.NotCompleted -> {}
                ResetChallengeStateResult.Reset -> {}
            }
        }
    }
}