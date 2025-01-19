package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDateTime

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
}