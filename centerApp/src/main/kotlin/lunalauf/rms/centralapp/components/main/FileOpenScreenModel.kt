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
import lunalauf.rms.model.api.PreferencesState
import lunalauf.rms.model.api.SaveResult
import lunalauf.rms.utilities.network.bot.BotManager
import kotlin.time.Duration.Companion.seconds

class FileOpenScreenModel(
    private val modelManager: ModelManager.Available,
    private val botManager: BotManager,
    modelState: ModelState.Loaded,
    snackBarHostState: SnackbarHostState
) : AbstractScreenModel(modelState) {
    private val _preferences =
        MutableStateFlow(PreferencesState(roundThreshold = event.roundThreshold.value.toFloat()))
    val preferences get() = _preferences.asStateFlow()

    private val autoSaveTimer = Timer(
        name = "auto-save timer",
        launcher = { action -> launchInModelScope { action() } },
        onError = { _preferences.update { it.copy(autoSaveActive = false) } }
    ) {
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
            event.setRoundThreshold(preferences.value.roundThreshold.toInt())
        }
    }

    fun updateSaveConnectionsActive(active: Boolean) {
        if (botManager is BotManager.Available) {
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