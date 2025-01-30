package lunalauf.rms.centralapp.components.main

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import lunalauf.rms.centralapp.components.AbstractStatelessScreenModel
import lunalauf.rms.centralapp.components.commons.showSnackbar
import lunalauf.rms.centralapp.utils.showNewFileChooser
import lunalauf.rms.centralapp.utils.showOpenFileChooser
import lunalauf.rms.modelapi.ModelAPI
import lunalauf.rms.modelapi.ModelState
import lunalauf.rms.modelapi.resource.ModelResourceManager
import lunalauf.rms.modelapi.resource.ModelResult
import lunalauf.rms.modelapi.resource.SaveResult
import lunalauf.rms.utilities.network.bot.BotManager
import lunalauf.rms.utilities.network.server.NetworkManager
import org.eclipse.emf.common.util.URI
import java.time.LocalDate

class MainScreenModel : AbstractStatelessScreenModel() {

    val modelResourceManager = ModelResourceManager.initialize()
    private val _modelState: MutableStateFlow<ModelState> = MutableStateFlow(ModelState.Unloaded)
    val modelState = _modelState.asStateFlow()
    val networkManager = NetworkManager.initialize(modelState)
    val botManager = BotManager.initialize(modelState)

    val snackBarHostState = SnackbarHostState()

    fun newFile() {
        if (modelResourceManager is ModelResourceManager.Accessible) {
            val path = showNewFileChooser()
            if (path != null) {
                _modelState.value = ModelState.Loading
                launchInModelScope {
                    val result = modelResourceManager.newFile(URI.createFileURI(path), LocalDate.now().year)
                    _modelState.value = when (result) {
                        is ModelResult.Available -> {
                            result.modelState
                        }

                        is ModelResult.Error -> {
                            launchInDefaultScope {
                                snackBarHostState.showSnackbar(
                                    message = result.message,
                                    withDismissAction = true,
                                    duration = SnackbarDuration.Indefinite
                                )
                            }
                            ModelState.Unloaded
                        }
                    }
                }
            }
        }
    }

    fun openFile() {
        if (modelResourceManager is ModelResourceManager.Accessible) {
            val path = showOpenFileChooser()
            if (path != null) {
                _modelState.value = ModelState.Loading
                launchInModelScope {
                    val result = modelResourceManager.load(URI.createFileURI(path))
                    _modelState.value = when (result) {
                        is ModelResult.Available -> {
                            result.modelState
                        }

                        is ModelResult.Error -> {
                            launchInDefaultScope {
                                snackBarHostState.showSnackbar(
                                    message = result.message,
                                    withDismissAction = true,
                                    duration = SnackbarDuration.Indefinite,
                                    isError = true
                                )
                            }
                            ModelState.Unloaded
                        }
                    }
                }
            }
        }
    }

    fun closeFile() {
        if (modelResourceManager is ModelResourceManager.Accessible) {
            val loadedModelState = _modelState.value
            _modelState.value = ModelState.Loading
            launchInModelScope {
                val result = modelResourceManager.save(modelState.value)
                _modelState.value = when (result) {
                    is SaveResult.Success, SaveResult.NoFileOpen -> {
                        modelResourceManager.close()
                        ModelState.Unloaded
                    }

                    is SaveResult.Error -> {
                        snackBarHostState.showSnackbar(
                            message = result.message,
                            withDismissAction = true,
                            duration = SnackbarDuration.Indefinite,
                            isError = true
                        )
                        loadedModelState
                    }
                }
            }
        }
    }

    fun saveFile() {
        if (modelResourceManager is ModelResourceManager.Accessible) {
            launchInModelScope {
                when (val result = modelResourceManager.save(modelState.value)) {
                    is SaveResult.Error -> launchInDefaultScope {
                        snackBarHostState.showSnackbar(
                            message = result.message,
                            withDismissAction = true,
                            duration = SnackbarDuration.Indefinite,
                            isError = true
                        )
                    }

                    SaveResult.NoFileOpen -> _modelState.value = ModelState.Unloaded
                    is SaveResult.Success -> launchInDefaultScope {
                        snackBarHostState.showSnackbar(
                            message = "Model saved to file: ${result.uri.path()}",
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
        ModelAPI.freeResources()
    }
}