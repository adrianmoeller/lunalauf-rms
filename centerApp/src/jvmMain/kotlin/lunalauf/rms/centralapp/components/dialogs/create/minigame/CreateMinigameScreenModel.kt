package lunalauf.rms.centralapp.components.dialogs.create.minigame

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.centralapp.components.commons.showSnackbar
import lunalauf.rms.centralapp.utils.InputValidator
import lunalauf.rms.modelapi.CreateMinigameResult
import lunalauf.rms.modelapi.ModelState

class CreateMinigameScreenModel(
    private val modelState: ModelState.Loaded,
    private val snackBarHostState: SnackbarHostState
) : AbstractScreenModel(modelState) {
    var id by mutableStateOf("")
        private set
    var idValid by mutableStateOf(false)
        private set
    var name by mutableStateOf("")
        private set
    var nameValid by mutableStateOf(false)
        private set
    var processing by mutableStateOf(false)
        private set

    fun updateId(id: String) {
        val trimmedId = id.trim()
        val parsedId = trimmedId.toIntOrNull()?.takeIf { it >= 0 && !modelState.minigames.value.ids.contains(it) }
        idValid = parsedId != null
        this.id = trimmedId
    }

    fun updateName(name: String) {
        nameValid = InputValidator.validateName(name) && name.isNotBlank()
        this.name = name
    }

    fun createMinigame(onClose: () -> Unit) {
        processing = true
        launchInModelScope {
            when (modelAPI.createMinigame(name, id.toInt())) {
                CreateMinigameResult.BlankName -> {
                    launchInDefaultScope {
                        snackBarHostState.showSnackbar(
                            message = "Minigames need a non-blank name",
                            withDismissAction = true,
                            duration = SnackbarDuration.Long,
                            isError = true
                        )
                    }
                }

                is CreateMinigameResult.Exists -> {
                    launchInDefaultScope {
                        snackBarHostState.showSnackbar(
                            message = "A minigame with this ID already exists",
                            withDismissAction = true,
                            duration = SnackbarDuration.Long,
                            isError = true
                        )
                    }
                }

                is CreateMinigameResult.Created -> {
                    launchInDefaultScope {
                        snackBarHostState.showSnackbar(
                            message = "Created minigame '$name'",
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