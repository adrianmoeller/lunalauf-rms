package lunalauf.rms.centralapp.ui.components.dialogs.createrunner

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import lunalauf.rms.centralapp.ui.components.AbstractScreenModel
import lunalauf.rms.centralapp.util.InputValidator
import lunalauf.rms.modelapi.CreateRunnerResult
import lunalauf.rms.modelapi.ModelState

class EnterNameScreenModel(
    modelState: ModelState.Loaded,
    private val id: ULong
) : ScreenModel, AbstractScreenModel() {
    private val modelAPI = modelState.modelAPI

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
        launchInDefaultScope {
            when (modelAPI.createRunner(id, name)) {
                is CreateRunnerResult.Created -> {
                    // TODO
                }

                is CreateRunnerResult.Exists -> {
                    // TODO
                }
            }
            onClose()
        }
    }
}