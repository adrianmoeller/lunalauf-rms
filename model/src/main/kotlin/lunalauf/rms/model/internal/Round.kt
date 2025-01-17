package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDateTime

class Round internal constructor(
    points: Int,
    timestamp: LocalDateTime,
    manuallyLogged: Boolean,
    runner: Runner
) : LogEntry(
    points,
    timestamp
) {
    private val _manuallyLogged = MutableStateFlow(manuallyLogged)
    val manuallyLogged get() = _manuallyLogged.asStateFlow()

    private val _runner = MutableStateFlow(runner)
    val runner get() = _runner.asStateFlow()

    private val _team = MutableStateFlow<Team?>(null)
    val team get() = _team.asStateFlow()

    internal fun initSetTeam(team: Team?) {
        _team.update { team }
    }
}