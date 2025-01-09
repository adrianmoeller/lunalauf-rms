package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class Event internal constructor(
    year: Int,
    runDuration: Int,
    sponsorPoolAmount: Double,
    sponsorPoolRounds: Int,
    additionalContribution: Double,
    teams: List<Team>,
    runners: List<Runner>,
    minigames: List<Minigame>,
    challenges: List<Challenge>,
    connections: List<ConnectionEntry>
) {
    private val _year = MutableStateFlow(year)
    val year get() = _year.asStateFlow()

    private val _runDuration = MutableStateFlow(runDuration)
    val runDuration get() = _runDuration.asStateFlow()

    private val _sponsorPoolAmount = MutableStateFlow(sponsorPoolAmount)
    val sponsorPoolAmount get() = _sponsorPoolAmount.asStateFlow()

    private val _sponsorPoolRounds = MutableStateFlow(sponsorPoolRounds)
    val sponsorPoolRounds get() = _sponsorPoolRounds.asStateFlow()

    private val _additionalContribution = MutableStateFlow(additionalContribution)
    val additionalContribution get() = _additionalContribution.asStateFlow()

    private val _teams = MutableStateFlow(teams)
    val teams get() = _teams.asStateFlow()

    private val _runners = MutableStateFlow(runners)
    val runners get() = _runners.asStateFlow()

    private val _minigames = MutableStateFlow(minigames)
    val minigames get() = _minigames.asStateFlow()

    private val _challenges = MutableStateFlow(challenges)
    val challenges get() = _challenges.asStateFlow()

    private val _connections = MutableStateFlow(connections)
    val connections get() = _connections.asStateFlow()
}
