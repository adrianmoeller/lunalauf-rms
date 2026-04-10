package lunalauf.rms.model.serialization

import kotlinx.serialization.Serializable

@Serializable
data class ConnectionEntrySM(
    val chatId: Long,
    val runnerUid: Long
)