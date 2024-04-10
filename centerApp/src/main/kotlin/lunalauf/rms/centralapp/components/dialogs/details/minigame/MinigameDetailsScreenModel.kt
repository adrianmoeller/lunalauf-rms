package lunalauf.rms.centralapp.components.dialogs.details.minigame

import LunaLaufLanguage.Minigame
import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.centralapp.utils.InputValidator
import lunalauf.rms.modelapi.ModelState
import lunalauf.rms.modelapi.UpdateMinigameIdResult
import lunalauf.rms.modelapi.UpdateMinigameNameResult

class MinigameDetailsScreenModel(
    private val modelState: ModelState.Loaded
) : AbstractScreenModel(modelState) {
    fun validateId(id: String): Int? {
        return id.trim().toIntOrNull()?.takeIf { it >= 0 && !modelState.minigames.value.ids.contains(it) }
    }

    fun updateId(minigame: Minigame, id: Int) {
        launchInModelScope {
            when (modelAPI.updateMinigameId(minigame, id)) {
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
            when (modelAPI.updateMinigameName(minigame, name)) {
                UpdateMinigameNameResult.BlankName -> {}
                UpdateMinigameNameResult.Updated -> {}
            }
        }
    }
}