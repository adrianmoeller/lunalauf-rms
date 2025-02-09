package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import lunalauf.rms.model.api.AddRunnerToTeamResult
import lunalauf.rms.model.api.DeleteElementResult
import lunalauf.rms.model.api.LogMinigameResultResult
import lunalauf.rms.model.common.ContributionType
import lunalauf.rms.model.helper.Timestamps
import org.slf4j.LoggerFactory

class Team internal constructor(
    event: Event,
    name: String,
    amountPerRound: Double,
    amountFix: Double,
    contributionType: ContributionType
) : Contributor(
    event,
    name,
    amountPerRound,
    amountFix,
    contributionType
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val _members = MutableStateFlow(emptyList<Runner>())
    val members get() = _members.asStateFlow()

    private val _funfactorResults = MutableStateFlow(emptyList<FunfactorResult>())
    val funfactorResults get() = _funfactorResults.asStateFlow()

    val numOfFunfactorPoints = funfactorResults.map { it.sumOf { result -> result.points.value } }
        .stateIn(event.scope, SharingStarted.Eagerly, 0)

    val totalAmount = combine(
        super.contributionType,
        super.amountPerRound,
        super.amountFix,
        numOfRounds,
        numOfFunfactorPoints
    ) { contributionType, amountPerRound, amountFix, numOfRounds, numOfFunfactorPoints ->
        when (contributionType) {
            ContributionType.FIXED -> amountFix
            ContributionType.PER_ROUND -> amountPerRound * (numOfRounds + numOfFunfactorPoints)
            ContributionType.BOTH -> amountFix + amountPerRound * (numOfRounds + numOfFunfactorPoints)
            ContributionType.NONE -> 0.0
        }
    }.stateIn(event.scope, SharingStarted.Eagerly, 0.0)

    internal constructor(
        event: Event,
        name: String
    ) : this(
        event = event,
        name = name,
        amountPerRound = 0.0,
        amountFix = 0.0,
        contributionType = ContributionType.NONE
    )

    internal fun internalSetMembers(members: List<Runner>) {
        this._members.update { members }
    }

    internal fun internalSetFunfactorResults(funfactorResults: List<FunfactorResult>) {
        this._funfactorResults.update { funfactorResults }
    }

    internal fun internalRemoveRunner(runner: Runner) {
        _members.update { it - runner }
    }

    internal fun internalRemoveFunfactorResult(result: FunfactorResult) {
        _funfactorResults.update { it - result }
    }

    suspend fun addRunner(runner: Runner): AddRunnerToTeamResult {
        event.mutex.withLock {
            val oldTeam = runner.team.value

            if (this == oldTeam) {
                logger.warn("Missing UI check if runner is already a team member")
                return AddRunnerToTeamResult.AlreadyMember
            }

            oldTeam?._members?.update { it - runner }
            this._members.update { it + runner }
            runner.internalSetTeam(this)

            logger.info("Added {} to {}", runner, this)
            return AddRunnerToTeamResult.Added
        }
    }

    suspend fun getRoundDurations(): List<Long> {
        event.mutex.withLock {
            return rounds.value
                .filterNot { it.manuallyLogged.value }
                .map { it.timestamp.value.toInstant(TimeZone.UTC).toEpochMilliseconds() }
                .sorted()
                .zipWithNext { a, b -> b - a }
        }
    }

    private fun internalLogFunfactorResult(type: Funfactor, points: Int): FunfactorResult {
        val newFunfactorResult = FunfactorResult(
            event = event,
            points = points,
            timestamp = Timestamps.now(),
            team = this,
            type = type
        )
        _funfactorResults.update { it + newFunfactorResult }

        logger.info("Logged {}", newFunfactorResult)
        return newFunfactorResult
    }

    suspend fun logFunfactorResult(type: Funfactor, points: Int): FunfactorResult {
        event.mutex.withLock {
            return internalLogFunfactorResult(type, points)
        }
    }

    suspend fun logMinigameResult(minigameID: Int, points: Int): LogMinigameResultResult {
        event.mutex.withLock {
            val minigame = event.internalGetMinigame(minigameID)
            if (minigame == null) {
                logger.warn("There is no minigame with ID '{}'", minigameID)
                return LogMinigameResultResult.NoMinigameWithId
            }

            val funfactorResult = internalLogFunfactorResult(minigame, points)

            return LogMinigameResultResult.Logged(funfactorResult)
        }
    }

    override suspend fun delete(): DeleteElementResult {
        event.mutex.withLock {
            if (funfactorResults.value.isNotEmpty())
                return DeleteElementResult.NotDeleted("Team has Funfactor results")

            members.value.forEach { it.internalSetTeam(null) }
            event.internalRemoveTeam(this)

            return DeleteElementResult.Deleted
        }
    }
}
