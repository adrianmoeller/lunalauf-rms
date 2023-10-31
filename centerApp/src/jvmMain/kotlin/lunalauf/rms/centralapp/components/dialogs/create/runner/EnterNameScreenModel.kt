package lunalauf.rms.centralapp.components.dialogs.create.runner

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.centralapp.components.commons.showSnackbar
import lunalauf.rms.centralapp.utils.InputValidator
import lunalauf.rms.modelapi.CreateRunnerResult
import lunalauf.rms.modelapi.ModelState

class EnterNameScreenModel(
    modelState: ModelState.Loaded,
    private val id: ULong,
    private val snackBarHostState: SnackbarHostState
) : ScreenModel, AbstractScreenModel(modelState) {
    var name by mutableStateOf("")
        private set
    var nameValid by mutableStateOf(true)
        private set
    var processing by mutableStateOf(false)
        private set

    fun updateName(name: String) {
        nameValid = InputValidator.validateName(name)
        this.name = name
    }

    fun createRunner(onClose: () -> Unit) {
        processing = true
        launchInModelScope {
            when (modelAPI.createRunner(id, name)) {
                is CreateRunnerResult.Created -> {
                    launchInDefaultScope {
                        snackBarHostState.showSnackbar(
                            message = "Created runner '$name' with ID: $id",
                            withDismissAction = true,
                            duration = SnackbarDuration.Short
                        )
                    }
                }

                is CreateRunnerResult.Exists -> {
                    launchInDefaultScope {
                        snackBarHostState.showSnackbar(
                            message = "A runner with this ID already exists",
                            withDismissAction = true,
                            duration = SnackbarDuration.Long,
                            isError = true
                        )
                    }
                }
            }
            onClose()
        }
    }
}