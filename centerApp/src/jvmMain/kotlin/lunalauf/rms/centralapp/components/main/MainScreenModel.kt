package lunalauf.rms.centralapp.components.main

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import lunalauf.rms.centralapp.components.AbstractScreenModel
import lunalauf.rms.centralapp.components.commons.showSnackbar
import lunalauf.rms.centralapp.utils.showNewFileChooser
import lunalauf.rms.centralapp.utils.showOpenFileChooser
import lunalauf.rms.modelapi.ModelState
import lunalauf.rms.modelapi.resource.ModelResourceManager
import lunalauf.rms.modelapi.resource.ModelResult
import lunalauf.rms.modelapi.resource.SaveResult
import org.eclipse.emf.common.util.URI
import java.time.LocalDate

class MainScreenModel : AbstractScreenModel() {

    val modelResourceManager = ModelResourceManager.initialize()
    var modelState: ModelState by mutableStateOf(ModelState.Unloaded)
        private set
    val snackBarHostState = SnackbarHostState()

    fun newFile() {
        if (modelResourceManager is ModelResourceManager.Accessible) {
            val path = showNewFileChooser()
            if (path != null) {
                modelState = ModelState.Loading
                launchInModelScope {
                    val result = modelResourceManager.newFile(URI.createFileURI(path), LocalDate.now().year)
                    modelState = when (result) {
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
                modelState = ModelState.Loading
                launchInModelScope {
                    val result = modelResourceManager.load(URI.createFileURI(path))
                    modelState = when (result) {
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
            val loadedModelState = modelState
            modelState = ModelState.Loading
            launchInModelScope {
                val result = modelResourceManager.save()
                modelState = when (result) {
                    is SaveResult.Success, SaveResult.NoFileOpen -> {
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
                when (val result = modelResourceManager.save()) {
                    is SaveResult.Error -> launchInDefaultScope {
                        snackBarHostState.showSnackbar(
                            message = result.message,
                            withDismissAction = true,
                            duration = SnackbarDuration.Indefinite,
                            isError = true
                        )
                    }

                    SaveResult.NoFileOpen -> modelState = ModelState.Unloaded
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
}