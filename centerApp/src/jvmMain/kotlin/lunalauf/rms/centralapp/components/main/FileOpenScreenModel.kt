package lunalauf.rms.centralapp.components.main

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.centralapp.utils.Timer
import lunalauf.rms.modelapi.ModelState
import lunalauf.rms.modelapi.resource.ModelResourceManager
import lunalauf.rms.modelapi.resource.SaveResult
import lunalauf.rms.modelapi.states.PreferencesState
import kotlin.time.Duration.Companion.seconds

class FileOpenScreenModel(
    private val modelResourceManager: ModelResourceManager,
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
        if (modelResourceManager is ModelResourceManager.Accessible) {
            when (val result = modelResourceManager.save()) {
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
        _preferences.update { it.copy(saveConnectionsActive = active) }

        TODO()
    }
}