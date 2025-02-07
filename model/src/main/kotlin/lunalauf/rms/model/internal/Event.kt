package lunalauf.rms.model.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import lunalauf.rms.model.api.CreateChallengeResult
import lunalauf.rms.model.api.CreateMinigameResult
import lunalauf.rms.model.api.CreateRunnerResult
import lunalauf.rms.model.api.CreateTeamResult
import lunalauf.rms.model.common.ChallengeState
import org.slf4j.LoggerFactory

private const val CREATED_LOG_MSG = "Created {}"

class Event internal constructor(
    year: Int,
    runDuration: Int,
    sponsorPoolAmount: Double,
    sponsorPoolRounds: Int,
    additionalContribution: Double,
    roundThreshold: Int
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    internal val scope = CoroutineScope(Dispatchers.Default)
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

    private val overallRunnerRounds = combine(runners.value.map { it.numOfRounds }) { it.sum() }
    private val overallFunfactorPoints = combine(teams.value.map { it.numOfFunfactorPoints }) { it.sum() }

    val overallRounds = combine(overallRunnerRounds, overallFunfactorPoints) { rounds, points -> rounds + points }
        .stateIn(scope, SharingStarted.Eagerly, 0)

    private val teamsContribution = combine(teams.value.map { it.totalAmount }) { it.sum() }
    private val singleRunnersContribution = combine(
        runners.value.filter { it.team.value == null }.map { it.totalAmount }) { it.sum() }

    val runnersContribution = combine(
        teamsContribution,
        singleRunnersContribution,
        this.additionalContribution
    ) { teams, single, additional ->
        teams + single + additional
    }.stateIn(scope, SharingStarted.Eagerly, 0.0)

    val currentSponsorPoolAmount = combine(
        overallRounds,
        this.sponsorPoolAmount,
        this.sponsorPoolRounds
    ) { overall, amount, rounds ->
        calcCurrentSponsorPoolAmount(overall, amount, rounds)
    }.stateIn(scope, SharingStarted.Eagerly, 0.0)

    val overallContribution = combine(runnersContribution, currentSponsorPoolAmount) { contribution, amount ->
        contribution + amount
    }.stateIn(scope, SharingStarted.Eagerly, 0.0)

    private val _roundThreshold = MutableStateFlow(roundThreshold)
    val roundThreshold get() = _roundThreshold.asStateFlow()

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

    internal val chipIdToRunner: MutableMap<Long, Runner> = mutableMapOf()
    internal val nameToTeam: MutableMap<String, Team> = mutableMapOf()
    internal val idToMinigame: MutableMap<Int, Minigame> = mutableMapOf()

    constructor(year: Int) : this(
        year = year,
        runDuration = 150,
        sponsorPoolAmount = 0.0,
        sponsorPoolRounds = 0,
        additionalContribution = 0.0,
        roundThreshold = 40
    )

    internal fun internalSetTeams(teams: List<Team>) {
        this._teams.update { teams }
        this.nameToTeam.putAll(teams.associateBy { it.name.value })
    }

    internal fun internalSetRunners(runners: List<Runner>) {
        this._runners.update { runners }
        this.chipIdToRunner.putAll(runners.associateBy { it.chipId.value })
    }

    internal fun internalSetMinigames(minigames: List<Minigame>) {
        this._minigames.update { minigames }
    }

    internal fun internalSetChallenges(challenges: List<Challenge>) {
        this._challenges.update { challenges }
    }

    internal fun internalSetConnections(connections: List<ConnectionEntry>) {
        this._connections.update { connections }
    }

    internal fun internalSetRunTimer(runTimer: RunTimer) {
        this.runTimer = runTimer
    }

    internal fun internalRemoveRunner(runner: Runner) {
        this._runners.update { it - runner }
    }

    internal fun internalRemoveTeam(team: Team) {
        this._teams.update { it - team }
    }

    internal fun internalRemoveMinigame(minigame: Minigame) {
        this._minigames.update { it - minigame }
    }

    internal fun internalRemoveChallenge(challenge: Challenge) {
        this._challenges.update { it - challenge }
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

    fun getRunner(chipId: Long): Runner? = chipIdToRunner[chipId]
    internal fun getTeam(name: String): Team? = nameToTeam[name]
    internal fun getMinigame(id: Int): Minigame? = idToMinigame[id]

    suspend fun createRunner(chipId: Long, name: String): CreateRunnerResult {
        mutex.withLock {
            getRunner(chipId)?.let {
                logger.warn("Missing UI check if chip ID already exists when creating a runner")
                return CreateRunnerResult.Exists(it)
            }

            val newRunner = Runner(
                event = this,
                chipId = chipId,
                name = name
            )
            this._runners.update { it + newRunner }
            this.chipIdToRunner[chipId] = newRunner

            logger.info(CREATED_LOG_MSG, newRunner)
            return CreateRunnerResult.Created(newRunner)
        }
    }

    suspend fun createTeam(name: String): CreateTeamResult {
        mutex.withLock {
            if (name.isBlank()) {
                logger.warn("Missing UI check if name is not blank when creating a team")
                return CreateTeamResult.BlankName
            }

            getTeam(name)?.let {
                logger.warn("Missing UI check if name already exists when creating a team")
                return CreateTeamResult.Exists(it)
            }

            val newTeam = Team(
                event = this,
                name = name
            )

            this._teams.update { it + newTeam }
            this.nameToTeam[name] = newTeam

            logger.info(CREATED_LOG_MSG, newTeam)
            return CreateTeamResult.Created(newTeam)
        }
    }

    suspend fun createMinigame(name: String, id: Int): CreateMinigameResult {
        mutex.withLock {
            getMinigame(id)?.let {
                logger.warn("Missing UI check if minigame ID already exists when creating a minigame")
                return CreateMinigameResult.Exists(it)
            }

            if (name.isBlank()) {
                logger.warn("Missing UI check if name is not blank when creating a minigame")
                return CreateMinigameResult.BlankName
            }

            val newMinigame = Minigame(
                event = this,
                name = name,
                description = "",
                id = id
            )

            this._minigames.update { it + newMinigame }
            this.idToMinigame[id] = newMinigame

            logger.info(CREATED_LOG_MSG, newMinigame)
            return CreateMinigameResult.Created(newMinigame)
        }
    }

    private fun internalCreateChallenge(
        name: String,
        description: String,
        expires: Boolean = false,
        expireMsg: String = "",
        duration: Int = 0,
        receiveImage: Boolean = false
    ): CreateChallengeResult {
        if (name.isBlank()) {
            logger.warn("Missing UI check if name is not blank when creating a challenge")
            return CreateChallengeResult.BlankName
        }

        val newChallenge = Challenge(
            event = this,
            name = name,
            description = description,
            expires = expires,
            expireMsg = expireMsg,
            duration = duration,
            state = ChallengeState.PENDING,
            receiveImages = receiveImage
        )

        this._challenges.update { it + newChallenge }

        logger.info(CREATED_LOG_MSG, newChallenge)
        return CreateChallengeResult.Created(newChallenge)
    }

    suspend fun createChallenge(name: String, description: String): CreateChallengeResult {
        mutex.withLock {
            return internalCreateChallenge(name, description)
        }
    }

    suspend fun createExpiringChallenge(
        name: String,
        description: String,
        duration: Int,
        expireMsg: String,
        receiveImage: Boolean
    ): CreateChallengeResult {
        mutex.withLock {
            if (duration < 0) {
                logger.warn("Missing UI check if duration is positive when creating a challenge")
                return CreateChallengeResult.NegativeDuration
            }

            return internalCreateChallenge(
                name = name,
                description = description,
                expires = true,
                expireMsg = expireMsg,
                duration = duration,
                receiveImage = receiveImage
            )
        }
    }

    suspend fun storeConnections(connectionId2Runner: Map<Long, Runner>) {
        mutex.withLock {
            _connections.update {
                connectionId2Runner.map { (id, runner) -> ConnectionEntry(this, id, runner) }
            }

            logger.info("Connections stored")
        }
    }

    private fun calcCurrentSponsorPoolAmount(
        overallRounds: Int,
        sponsorPoolAmount: Double,
        sponsorPoolRounds: Int
    ): Double {
        return if (overallRounds > sponsorPoolRounds) sponsorPoolAmount
        else if (sponsorPoolRounds == 0) 0.0
        else sponsorPoolAmount / sponsorPoolRounds * overallRounds
    }
}
