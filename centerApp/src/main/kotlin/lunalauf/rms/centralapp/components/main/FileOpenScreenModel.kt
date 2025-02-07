package lunalauf.rms.centralapp.components.main

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.centralapp.utils.Timer
import lunalauf.rms.model.api.ModelManager
import lunalauf.rms.model.api.ModelState
import lunalauf.rms.modelapi.resource.SaveResult
import lunalauf.rms.modelapi.states.PreferencesState
import lunalauf.rms.utilities.network.bot.BotManager
import kotlin.time.Duration.Companion.seconds

class FileOpenScreenModel(
    private val modelManager: ModelManager,
    private val botManager: BotManager,
    modelState: ModelState.Loaded,
    snackBarHostState: SnackbarHostState
) : AbstractScreenModel(modelState) {
    private val _preferences = MutableStateFlow(
        PreferencesState(roundThreshold = modelAPI.roundThreshold.toFloat())
    )
    val preferences get() = _preferences.asStateFlow()

    private val autoSaveTimer = Timer(
        name = "auto-save timer",
        launcher = { action -> launchInModelScope { action() } },
        onError = { _preferences.update { it.copy(autoSaveActive = false) } }
    ) {
        if (modelManager is ModelResourceManager.Accessible) {
            when (val result = modelManager.save()) {
                is SaveResult.Error -> {
                    launchInDefaultScope {
                        snackBarHostState.showSnackbar(
                            message = result.message,
                            withDismissAction = true,
                            duration = SnackbarDuration.Indefinite
                        )
                    }
                    throw Exception("Terminate task")
                }

                is SaveResult.NoFileOpen -> throw Exception("Terminate task")
                is SaveResult.Success -> {}
            }
        } else {
            throw Exception("Terminate task")
        }
    }

    fun updateAutoSaveActive(active: Boolean) {
        _preferences.update { it.copy(autoSaveActive = active) }
        if (active)
            autoSaveTimer.restart(period = preferences.value.autoSaveInterval.toInt().seconds)
        else
            autoSaveTimer.stop()
    }

    fun updateDisplayedAutoSaveInterval(interval: Float) {
        _preferences.update { it.copy(autoSaveInterval = interval) }
    }

    fun updateAutoSaveInterval() {
        autoSaveTimer.restart(period = preferences.value.autoSaveInterval.toInt().seconds)
    }

    fun updateDisplayedRoundThreshold(threshold: Float) {
        _preferences.update { it.copy(roundThreshold = threshold) }
    }

    fun updateRoundThreshold() {
        launchInModelScope {
            modelAPI.setRoundThreshold(preferences.value.roundThreshold.toInt())
        }
    }

    fun updateSaveConnectionsActive(active: Boolean) {
        if (modelManager is ModelResourceManager.Accessible && botManager is BotManager.Available) {
            _preferences.update { it.copy(saveConnectionsActive = active) }

            launchInDefaultScope {
                if (active) {
                    modelManager.setPreSaveProcessing { botManager.saveConnectionData() }
                } else {
                    modelManager.removePreSaveProcessing()
                }
            }
        } else {
            _preferences.update { it.copy(saveConnectionsActive = false) }
        }
    }
}