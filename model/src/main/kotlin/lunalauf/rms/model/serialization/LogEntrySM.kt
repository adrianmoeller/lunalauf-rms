package lunalauf.rms.model.serialization

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
sealed class LogEntrySM {
    abstract val points: Int
    abstract val timestamp: LocalDateTime
}
