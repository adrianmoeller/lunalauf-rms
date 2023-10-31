package lunalauf.rms.centralapp.components.dialogs

import LunaLaufLanguage.Runner
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import lunalauf.rms.modelapi.ModelState

class ScanChipScreenModel(
    private val modelState: ModelState.Loaded
) : ScreenModel {
    private var idBuffer = StringBuilder()
    var showError by mutableStateOf(false)
        private set

    fun toIdBuffer(key: Int) {
        idBuffer.append(key)
    }

    fun processBufferedId(
        onKnown: (Runner) -> Unit,
        onUnknown: (Long) -> Unit
    ) {
        val scannedId = idBuffer.toString().toLongOrNull()
        idBuffer.clear()

        if (scannedId == null) {
            showError = true
            return
        }

        val runner = modelState.runners.value.id2runners[scannedId.toLong()]
        if (runner != null)
            onKnown(runner)
        else
            onUnknown(scannedId)
        showError = false
    }
}
