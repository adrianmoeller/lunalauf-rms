package lunalauf.rms.centralapp.components.dialogs.details.minigame

import kotlinx.coroutines.runBlocking
import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.centralapp.utils.InputValidator
import lunalauf.rms.model.api.ModelState
import lunalauf.rms.model.api.UpdateFunfactorNameResult
import lunalauf.rms.model.api.UpdateMinigameIdResult
import lunalauf.rms.model.internal.Minigame

class MinigameDetailsScreenModel(
    private val modelState: ModelState.Loaded
) : AbstractScreenModel(modelState) {
    fun validateId(id: String): Int? {
        return runBlocking(ModelState.modelContext) {
            id.trim().toIntOrNull()?.takeIf {
                it >= 0 && event.getMinigame(it) == null
            }
        }
    }

    fun updateId(minigame: Minigame, id: Int) {
        launchInModelScope {
            when (minigame.updateId(id)) {
                is UpdateMinigameIdResult.Exists -> {}
                UpdateMinigameIdResult.Updated -> {}
            }
        }
    }

    fun validateName(name: String): String? {
        return name.takeIf { InputValidator.validateName(it) && it.isNotBlank() }
    }

    fun updateName(minigame: Minigame, name: String) {
        launchInModelScope {
            when (minigame.updateName(name)) {
                UpdateFunfactorNameResult.BlankName -> {}
                UpdateFunfactorNameResult.Updated -> {}
            }
        }
    }
}