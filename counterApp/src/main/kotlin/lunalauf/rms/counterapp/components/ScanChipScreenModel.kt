package lunalauf.rms.counterapp.components

class ScanChipScreenModel : AbstractScreenModel() {
    private var idBuffer = StringBuilder()

    fun toIdBuffer(key: Int) {
        idBuffer.append(key)
    }

    fun getBufferedId(): Long? {
        val scannedId = idBuffer.toString().toLongOrNull()
        idBuffer.clear()
        return scannedId
    }
}