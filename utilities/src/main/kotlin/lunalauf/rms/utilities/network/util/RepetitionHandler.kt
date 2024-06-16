package lunalauf.rms.utilities.network.util

class RepetitionHandler(
    private val repetitionTimeThreshold: Long
) {
    private var lastId: Long = -1
    private var lastTimeMillis: Long = 0

    fun isUnwantedRepetition(id: Long): Boolean {
        if (id == lastId)
            if (System.currentTimeMillis() - lastTimeMillis < repetitionTimeThreshold)
                return true
        lastId = id
        lastTimeMillis = System.currentTimeMillis()
        return false
    }
}
