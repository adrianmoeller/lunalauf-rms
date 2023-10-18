package lunalauf.rms.centralapp.components.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import lunalauf.rms.centralapp.components.AbstractScreenModel
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

    fun newFile() {
        if (modelResourceManager is ModelResourceManager.Accessible) {
            val path = showNewFileChooser()
            if (path != null) {
                modelState = ModelState.Loading
                launchInDefaultScope {
                    val result = modelResourceManager.newFile(URI.createFileURI(path), LocalDate.now().year)
                    modelState = if (result is ModelResult.Available)
                        result.modelState
                    else
                        ModelState.Unloaded
                }
            }
        }
    }

    fun openFile() {
        if (modelResourceManager is ModelResourceManager.Accessible) {
            val path = showOpenFileChooser()
            if (path != null) {
                modelState = ModelState.Loading
                launchInDefaultScope {
                    val result = modelResourceManager.load(URI.createFileURI(path))
                    modelState = if (result is ModelResult.Available)
                        result.modelState
                    else
                        ModelState.Unloaded
                }
            }
        }
    }

    fun closeFile() {
        if (modelResourceManager is ModelResourceManager.Accessible) {
            val loadedModelState = modelState
            modelState = ModelState.Loading
            launchInDefaultScope {
                val result = modelResourceManager.save()
                modelState = when (result) {
                    is SaveResult.Success, SaveResult.NoFileOpen -> ModelState.Unloaded
                    is SaveResult.Error -> loadedModelState
                }
            }
        }
    }

    fun saveFile() {
        if (modelResourceManager is ModelResourceManager.Accessible) {
            launchInDefaultScope {
                val result = modelResourceManager.save()
                when (result) {
                    is SaveResult.Error -> TODO()
                    SaveResult.NoFileOpen -> modelState = ModelState.Unloaded
                    is SaveResult.Success -> TODO()
                }
            }
        }
    }
}