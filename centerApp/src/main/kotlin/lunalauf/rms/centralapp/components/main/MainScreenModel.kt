package lunalauf.rms.centralapp.components.main

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import lunalauf.rms.centralapp.components.AbstractStatelessScreenModel
import lunalauf.rms.centralapp.components.commons.showSnackbar
import lunalauf.rms.centralapp.utils.showNewFileChooser
import lunalauf.rms.centralapp.utils.showOpenFileChooser
import lunalauf.rms.model.api.ModelManager
import lunalauf.rms.model.api.ModelResult
import lunalauf.rms.model.api.ModelState
import lunalauf.rms.model.api.SaveResult
import lunalauf.rms.model.serialization.JsonPersistenceManager
import lunalauf.rms.utilities.network.bot.BotManager
import lunalauf.rms.utilities.network.server.NetworkManager
import java.time.LocalDate

class MainScreenModel : AbstractStatelessScreenModel() {

    val modelManager = ModelManager.initialize(JsonPersistenceManager())
    val networkManager = NetworkManager.initialize(modelManager)
    val botManager = BotManager.initialize(modelManager)

    val snackBarHostState = SnackbarHostState()

    fun newFile() {
        if (modelManager is ModelManager.Available) {
            val path = showNewFileChooser()

            if (path != null) {
                launchInModelScope {
                    val result = modelManager.new(path, LocalDate.now().year)
                    if (result is ModelResult.Error) {
                        launchInDefaultScope {
                            snackBarHostState.showSnackbar(
                                message = result.message,
                                withDismissAction = true,
                                duration = SnackbarDuration.Indefinite
                            )
                        }
                    }
                }
            }
        }
    }

    fun openFile() {
        if (modelManager is ModelManager.Available) {
            val path = showOpenFileChooser()

            if (path != null) {
                launchInModelScope {
                    val result = modelManager.load(path)
                    if (result is ModelResult.Error) {
                        launchInDefaultScope {
                            snackBarHostState.showSnackbar(
                                message = result.message,
                                withDismissAction = true,
                                duration = SnackbarDuration.Indefinite,
                                isError = true
                            )
                        }
                    }
                }
            }
        }
    }

    fun closeFile() {
        if (modelManager is ModelManager.Available) {
            launchInModelScope {
                when (val result = modelManager.save()) {
                    SaveResult.NoFileOpen -> {}
                    is SaveResult.Error -> {
                        snackBarHostState.showSnackbar(
                            message = result.message,
                            withDismissAction = true,
                            duration = SnackbarDuration.Indefinite,
                            isError = true
                        )
                    }

                    is SaveResult.Success -> {
                        modelManager.close()
                    }
                }
            }
        }
    }

    fun saveFile() {
        if (modelManager is ModelManager.Available) {
            launchInModelScope {
                when (val result = modelManager.save()) {
                    is SaveResult.Error -> launchInDefaultScope {
                        snackBarHostState.showSnackbar(
                            message = result.message,
                            withDismissAction = true,
                            duration = SnackbarDuration.Indefinite,
                            isError = true
                        )
                    }

                    SaveResult.NoFileOpen -> {}
                    is SaveResult.Success -> launchInDefaultScope {
                        snackBarHostState.showSnackbar(
                            message = "Model saved to file: ${result.path}",
                            withDismissAction = true,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
        }
    }

    fun onCloseApplication() {
        if (networkManager is NetworkManager.Available)
            networkManager.shutdown()
        if (botManager is BotManager.Available)
            botManager.shutdown()
        ModelState.freeResources()
    }
}