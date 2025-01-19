package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex

class Event internal constructor(
    year: Int,
    runDuration: Int,
    sponsorPoolAmount: Double,
    sponsorPoolRounds: Int,
    additionalContribution: Double
) {
    val mutex = Mutex()

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

    private val _teams = MutableStateFlow(emptyList<Team>())
    val teams get() = _teams.asStateFlow()

    private val _runners = MutableStateFlow(emptyList<Runner>())
    val runners get() = _runners.asStateFlow()

    private val _minigames = MutableStateFlow(emptyList<Minigame>())
    val minigames get() = _minigames.asStateFlow()

    private val _challenges = MutableStateFlow(emptyList<Challenge>())
    val challenges get() = _challenges.asStateFlow()

    private val _connections = MutableStateFlow(emptyList<ConnectionEntry>())
    val connections get() = _connections.asStateFlow()

    constructor(year: Int) : this(
        year = year,
        runDuration = 150,
        sponsorPoolAmount = 0.0,
        sponsorPoolRounds = 0,
        additionalContribution = 0.0
    )

    internal fun initSetTeams(teams: List<Team>) {
        this._teams.update { teams }
    }

    internal fun initSetRunners(runners: List<Runner>) {
        this._runners.update { runners }
    }

    internal fun initSetMinigames(minigames: List<Minigame>) {
        this._minigames.update { minigames }
    }

    internal fun initSetChallenges(challenges: List<Challenge>) {
        this._challenges.update { challenges }
    }

    internal fun initSetConnections(connections: List<ConnectionEntry>) {
        this._connections.update { connections }
    }
}
