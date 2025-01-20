package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import lunalauf.rms.model.api.AddRunnerToTeamResult
import lunalauf.rms.model.api.RemoveRunnerFromTeamResult
import lunalauf.rms.model.common.ContributionType
import org.slf4j.LoggerFactory

class Team internal constructor(
    event: Event,
    amountPerRound: Double,
    amountFix: Double,
    contributionType: ContributionType,
    name: String
) : Contributor(
    event,
    amountPerRound,
    amountFix,
    contributionType
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val _name = MutableStateFlow(name)
    val name get() = _name.asStateFlow()

    private val _members = MutableStateFlow(emptyList<Runner>())
    val members get() = _members.asStateFlow()

    private val _rounds = MutableStateFlow(emptyList<Round>())
    val rounds get() = _rounds.asStateFlow()

    private val _funfactorResults = MutableStateFlow(emptyList<FunfactorResult>())
    val funfactorResults get() = _funfactorResults.asStateFlow()

    val numOfRounds = rounds.map { it.sumOf { round -> round.points.value } }

    val numOfFunfactorPoints = funfactorResults.map { it.sumOf { result -> result.points.value } }

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
    }

    internal constructor(
        event: Event,
        name: String
    ) : this(
        event = event,
        amountPerRound = 0.0,
        amountFix = 0.0,
        contributionType = ContributionType.NONE,
        name = name
    )

    internal fun initSetMembers(members: List<Runner>) {
        this._members.update { members }
    }

    internal fun initSetRounds(rounds: List<Round>) {
        this._rounds.update { rounds }
    }

    internal fun initSetFunfactorResults(funfactorResults: List<FunfactorResult>) {
        this._funfactorResults.update { funfactorResults }
    }

    suspend fun updateName(name: String) {
        event.mutex.withLock {
            _name.update { name }
        }
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
            runner._team.update { this }

            logger.info("Added {} to {}", runner, this)
            return AddRunnerToTeamResult.Added
        }
    }

    suspend fun removeRunner(runner: Runner): RemoveRunnerFromTeamResult {
        event.mutex.withLock {
            val oldTeam = runner.team.value

            if (oldTeam == null) {
                logger.warn("Missing UI check if runner is already in no team")
                return RemoveRunnerFromTeamResult.AlreadyInNoTeam
            }

            this._members.update { it - runner }
            runner._team.update { null }

            logger.info("Removed {} from {}", runner, oldTeam)
            return RemoveRunnerFromTeamResult.Removed
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
}
