package lunalauf.rms.model.serialization

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class RoundSM(
    override val points: Int,
    override val timestamp: LocalDateTime,
    val manuallyLogged: Boolean
) : LogEntrySM()
