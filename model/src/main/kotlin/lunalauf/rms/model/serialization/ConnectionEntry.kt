package lunalauf.rms.model.serialization

import kotlinx.serialization.Serializable

@Serializable
data class ConnectionEntry(
    val chatId: Long,
    val runnerUid: Long
)