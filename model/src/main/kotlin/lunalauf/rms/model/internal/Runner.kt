package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import lunalauf.rms.model.api.UpdateRunnerIdResult
import lunalauf.rms.model.common.ContributionType
import org.slf4j.LoggerFactory

class Runner internal constructor(
    event: Event,
    amountPerRound: Double,
    amountFix: Double,
    contributionType: ContributionType,
    chipId: Long,
    name: String,
) : Contributor(
    event,
    amountPerRound,
    amountFix,
    contributionType
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val _chipId = MutableStateFlow(chipId)
    val chipId get() = _chipId.asStateFlow()

    private val _name = MutableStateFlow(name)
    val name get() = _name.asStateFlow()

    private val _team = MutableStateFlow<Team?>(null)
    val team get() = _team.asStateFlow()

    private val _rounds = MutableStateFlow(emptyList<Round>())
    val rounds get() = _rounds.asStateFlow()

    val numOfRounds = rounds.map { it.sumOf { round -> round.points.value } }

    val totalAmount = combine(
        super.contributionType,
        super.amountPerRound,
        super.amountFix,
        numOfRounds
    ) { contributionType, amountPerRound, amountFix, numOfRounds ->
        when (contributionType) {
            ContributionType.FIXED -> amountFix
            ContributionType.PER_ROUND -> amountPerRound * numOfRounds
            ContributionType.BOTH -> amountFix + amountPerRound * numOfRounds
            ContributionType.NONE -> 0.0
        }
    }

    internal constructor(
        event: Event,
        chipId: Long,
        name: String
    ) : this(
        event = event,
        amountPerRound = 0.0,
        amountFix = 0.0,
        contributionType = ContributionType.NONE,
        chipId = chipId,
        name = name,
    )

    internal fun initSetTeam(team: Team?) {
        _team.update { team }
    }

    internal fun initSetRounds(rounds: List<Round>) {
        _rounds.update { rounds }
    }

    suspend fun updateChipId(chipId: Long): UpdateRunnerIdResult {
        event.mutex.withLock {
            event.getRunner(chipId)?.let {
                logger.warn("Missing UI check if ID already exists when updating a runner ID")
                return UpdateRunnerIdResult.Exists(it)
            }

            _chipId.update { chipId }

            return UpdateRunnerIdResult.Updated
        }
    }

    suspend fun updateName(name: String) {
        event.mutex.withLock {
            _name.update { name }
        }
    }

    suspend fun getRoundDurations(): List<Long> {
        event.mutex.withLock {
            return rounds.value
                .asSequence()
                .filterNot { it.manuallyLogged.value }
                .map { it.timestamp.value.toInstant(TimeZone.UTC).toEpochMilliseconds() }
                .sorted()
                .zipWithNext { a, b -> b - a }
                .toList()
        }
    }
}
