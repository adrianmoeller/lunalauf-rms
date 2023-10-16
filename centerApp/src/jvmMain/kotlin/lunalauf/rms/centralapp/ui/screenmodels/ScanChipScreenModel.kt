package lunalauf.rms.centralapp.ui.screenmodels

import LunaLaufLanguage.Runner
import cafe.adriel.voyager.core.model.ScreenModel
import lunalauf.rms.modelapi.ModelState

class ScanChipScreenModel(
    private val modelState: ModelState.Loaded
) : ScreenModel {
    private var idBuffer = StringBuilder()

    fun toIdBuffer(key: UInt) {
        idBuffer.append(key)
    }

    fun processBufferedId(): IdResult {
        val scannedId = idBuffer.toString().toULongOrNull()
        idBuffer.clear()

        if (scannedId == null)
            return IdResult.Error

        val runner = modelState.runners.value.id2runners[scannedId.toLong()]
        return if (runner != null)
            IdResult.Known(runner)
        else
            IdResult.Unknown(scannedId)
    }
}

sealed class IdResult {
    data class Unknown(val id: ULong) : IdResult()
    data class Known(val runner: Runner) : IdResult()
    data object Error : IdResult() {
        const val message = "Invalid ID format"
    }
}
