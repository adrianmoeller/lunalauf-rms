package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDateTime
import lunalauf.rms.model.api.DeleteElementResult

class Round internal constructor(
    event: Event,
    points: Int,
    timestamp: LocalDateTime,
    manuallyLogged: Boolean,
    runner: Runner
) : LogEntry(
    event,
    points,
    timestamp
) {
    private val _manuallyLogged = MutableStateFlow(manuallyLogged)
    val manuallyLogged get() = _manuallyLogged.asStateFlow()

    private val _runner = MutableStateFlow(runner)
    val runner get() = _runner.asStateFlow()

    private val _team = MutableStateFlow<Team?>(null)
    val team get() = _team.asStateFlow()

    internal fun internalSetTeam(team: Team?) {
        _team.update { team }
    }

    override suspend fun delete(): DeleteElementResult {
        event.mutex.withLock {
            runner.value.internalRemoveRound(this)

            return DeleteElementResult.Deleted
        }
    }
}