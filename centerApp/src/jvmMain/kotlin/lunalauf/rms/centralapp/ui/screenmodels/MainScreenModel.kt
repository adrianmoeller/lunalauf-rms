package lunalauf.rms.centralapp.ui.screenmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import lunalauf.rms.centralapp.data.model.DataModel
import lunalauf.rms.centralapp.data.preferences.PreferencesState
import lunalauf.rms.centralapp.ui.filechooser.showNewFileChooser
import lunalauf.rms.centralapp.ui.filechooser.showOpenFileChooser
import lunalauf.rms.modelapi.LunaLaufAPI
import lunalauf.rms.modelapi.util.Result
import org.eclipse.emf.common.util.URI
import java.time.LocalDate

class MainScreenModel {
    var dataModelState: DataModel by mutableStateOf(DataModel.Unloaded)
        private set
    var preferencesState: PreferencesState by mutableStateOf(PreferencesState())
        private set

    fun newFile() {
        val path = showNewFileChooser()
        if (path != null) {
            dataModelState = DataModel.Loading
            val result = LunaLaufAPI.newFile(URI.createFileURI(path), LocalDate.now().year).log()
            dataModelState = if (result.hasResult())
                DataModel.Loaded(path, result.result!!)
            else
                DataModel.Unloaded
        }
    }

    fun openFile() {
        val path = showOpenFileChooser()
        if (path != null) {
            dataModelState = DataModel.Loading
            val result = LunaLaufAPI.load(URI.createFileURI(path)).log()
            dataModelState = if (result.hasResult())
                DataModel.Loaded(path, result.result!!)
            else
                DataModel.Unloaded
        }
    }

    fun closeFile() {
        val result = Result<Void>("Close File")

        val uri = result.makeSub(LunaLaufAPI.save())
        if (!uri.hasResult())
            result.failed("Failed saving file before closing", null).log()
        else
            dataModelState = DataModel.Unloaded
    }

     fun updateAutoSaveActive(value: Boolean) {

     }
}