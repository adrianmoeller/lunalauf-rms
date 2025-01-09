package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDateTime

class Round internal constructor(
    points: Int,
    timestamp: LocalDateTime,
    manuallyLogged: Boolean,
    runner: Runner,
    team: Team?
) : LogEntry(
    points,
    timestamp
) {
    private val _manuallyLogged = MutableStateFlow(manuallyLogged)
    val manuallyLogged get() = _manuallyLogged.asStateFlow()

    private val _runner = MutableStateFlow(runner)
    val runner get() = _runner.asStateFlow()

    private val _team = MutableStateFlow(team)
    val team get() = _team.asStateFlow()
}