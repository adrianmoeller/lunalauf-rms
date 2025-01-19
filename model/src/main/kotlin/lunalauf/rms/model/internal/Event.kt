package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import lunalauf.rms.model.api.CreateRunnerResult
import org.slf4j.LoggerFactory

class Event internal constructor(
    year: Int,
    runDuration: Int,
    sponsorPoolAmount: Double,
    sponsorPoolRounds: Int,
    additionalContribution: Double
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

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

    var runTimer: RunTimer = RunTimer()
        private set

    internal val chipId2Runner: MutableMap<Long, Runner> = mutableMapOf()

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
        this.chipId2Runner.putAll(runners.associateBy { it.chipId.value })
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

    internal fun initSetRunTimer(runTimer: RunTimer) {
        this.runTimer = runTimer
    }

    suspend fun setSponsoringPoolAmount(amount: Double) {
        mutex.withLock {
            _sponsorPoolAmount.update { amount }
        }
    }

    suspend fun setSponsoringPoolRounds(rounds: Int) {
        mutex.withLock {
            _sponsorPoolRounds.update { rounds }
        }
    }

    suspend fun updateAdditionalContribution(updateFunction: (Double) -> Double) {
        mutex.withLock {
            _additionalContribution.update { updateFunction(it) }
        }
    }

    internal fun getRunner(chipId: Long): Runner? = chipId2Runner[chipId]

    suspend fun createRunner(chipId: Long, name: String): CreateRunnerResult {
        mutex.withLock {
            getRunner(chipId)?.let {
                logger.warn("Missing UI check if ID already exists when creating a runner")
                return CreateRunnerResult.Exists(it)
            }

            val newRunner = Runner(
                event = this,
                chipId = chipId,
                name = name
            )
            this._runners.update { it + newRunner }

            logger.info("Created {}", newRunner)
            return CreateRunnerResult.Created(newRunner)
        }
    }
}
