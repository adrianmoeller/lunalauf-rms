package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDateTime
import lunalauf.rms.model.api.DeleteElementResult
import lunalauf.rms.model.helper.Formats

class FunfactorResult internal constructor(
    event: Event,
    points: Int,
    timestamp: LocalDateTime,
    team: Team,
    type: Funfactor
) : LogEntry(
    event,
    points,
    timestamp
) {
    private val _team = MutableStateFlow(team)
    val team get() = _team.asStateFlow()

    private val _type = MutableStateFlow(type)
    val type get() = _type.asStateFlow()

    override suspend fun delete(): DeleteElementResult {
        event.mutex.withLock {
            team.value.internalRemoveFunfactorResult(this)
            type.value.internalRemoveResult(this)

            return DeleteElementResult.Deleted
        }
    }

    override fun toString(): String {
        return "FunfactorResult(" +
                "timestamp=${Formats.timeFormat.format(timestamp.value)}, " +
                "type=${type.value.name}, team=${team.value.name})"
    }
}