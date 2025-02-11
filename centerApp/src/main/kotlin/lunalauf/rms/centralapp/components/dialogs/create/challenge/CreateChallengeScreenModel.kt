package lunalauf.rms.centralapp.components.dialogs.create.challenge

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.centralapp.components.commons.showSnackbar
import lunalauf.rms.centralapp.utils.InputValidator
import lunalauf.rms.model.api.CreateChallengeResult
import lunalauf.rms.model.api.ModelState

class CreateChallengeScreenModel(
    modelState: ModelState.Loaded,
    private val snackBarHostState: SnackbarHostState
) : AbstractScreenModel(modelState) {
    var name by mutableStateOf("")
        private set
    var nameValid by mutableStateOf(false)
        private set
    var description by mutableStateOf("")
        private set
    var expires by mutableStateOf(false)
        private set
    var duration by mutableStateOf("")
        private set
    var durationValid by mutableStateOf(false)
        private set
    var expireMessage by mutableStateOf("")
        private set
    var receiveImage by mutableStateOf(false)
        private set
    var processing by mutableStateOf(false)
        private set

    fun updateName(name: String) {
        nameValid = InputValidator.validateName(name) && name.isNotBlank()
        this.name = name
    }

    fun updateDescription(description: String) {
        this.description = description
    }

    fun updateExpires(expires: Boolean) {
        this.expires = expires
    }

    fun updateDuration(duration: String) {
        val trimmedDuration = duration.trim()
        val parsedDuration = trimmedDuration.toIntOrNull()?.takeIf { it >= 0 }
        durationValid = parsedDuration != null
        this.duration = trimmedDuration
    }

    fun updateExpireMessage(expireMessage: String) {
        this.expireMessage = expireMessage
    }

    fun updateReceiveImage(receiveImage: Boolean) {
        this.receiveImage = receiveImage
    }

    fun createChallenge(onClose: () -> Unit) {
        processing = true
        launchInModelScope {
            val result = if (expires) {
                event.createExpiringChallenge(name, description, duration.toInt(), expireMessage, receiveImage)
            } else {
                event.createChallenge(name, description)
            }
            when (result) {
                CreateChallengeResult.BlankName -> {
                    launchInDefaultScope {
                        snackBarHostState.showSnackbar(
                            message = "Challenges need a non-blank name",
                            withDismissAction = true,
                            duration = SnackbarDuration.Long,
                            isError = true
                        )
                    }
                }

                CreateChallengeResult.NegativeDuration -> {
                    launchInDefaultScope {
                        snackBarHostState.showSnackbar(
                            message = "Duration of a challenge must be a positive number",
                            withDismissAction = true,
                            duration = SnackbarDuration.Long,
                            isError = true
                        )
                    }
                }

                is CreateChallengeResult.Created -> {
                    launchInDefaultScope {
                        snackBarHostState.showSnackbar(
                            message = "Created challenge '$name'",
                            withDismissAction = true,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
            onClose()
        }
    }
}