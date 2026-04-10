package lunalauf.rms.model.serialization

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class FunfactorResultSM(
    override val points: Int,
    override val timestamp: LocalDateTime,
    val teamUid: Long
) : LogEntrySM()
