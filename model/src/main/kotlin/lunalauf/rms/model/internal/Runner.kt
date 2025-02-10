package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import lunalauf.rms.model.api.DeleteElementResult
import lunalauf.rms.model.api.LogRoundResult
import lunalauf.rms.model.api.RemoveRunnerFromTeamResult
import lunalauf.rms.model.api.UpdateRunnerIdResult
import lunalauf.rms.model.common.ContributionType
import lunalauf.rms.model.helper.Timestamps
import lunalauf.rms.model.helper.Timestamps.toMilliseconds
import org.slf4j.LoggerFactory

class Runner internal constructor(
    event: Event,
    name: String,
    amountPerRound: Double,
    amountFix: Double,
    contributionType: ContributionType,
    chipId: Long,
) : Contributor(
    event,
    name,
    amountPerRound,
    amountFix,
    contributionType
) {
    companion object {
        const val DEFAULT_ROUND_POINTS = 1
    }

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val _chipId = MutableStateFlow(chipId)
    val chipId get() = _chipId.asStateFlow()

    private val _team = MutableStateFlow<Team?>(null)
    val team get() = _team.asStateFlow()

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
    }.stateIn(event.scope, SharingStarted.Eagerly, 0.0)

    internal constructor(
        event: Event,
        chipId: Long,
        name: String
    ) : this(
        event = event,
        name = name,
        amountPerRound = 0.0,
        amountFix = 0.0,
        contributionType = ContributionType.NONE,
        chipId = chipId,
    )

    internal fun internalSetTeam(team: Team?) {
        _team.update { team }
    }


    suspend fun updateChipId(chipId: Long): UpdateRunnerIdResult {
        event.mutex.withLock {
            event.internalGetRunner(chipId)?.let {
                logger.warn("Missing UI check if ID already exists when updating a runner ID")
                return UpdateRunnerIdResult.Exists(it)
            }

            event.chipIdToRunner.remove(this.chipId.value)
            _chipId.update { chipId }
            event.chipIdToRunner[chipId] = this

            return UpdateRunnerIdResult.Updated
        }
    }

    suspend fun removeFromTeam(): RemoveRunnerFromTeamResult {
        event.mutex.withLock {
            val oldTeam = team.value

            if (oldTeam == null) {
                logger.warn("Missing UI check if runner is already in no team")
                return RemoveRunnerFromTeamResult.AlreadyInNoTeam
            }

            oldTeam.internalRemoveRounds(this.rounds.value.toSet())
            oldTeam.internalRemoveRunner(this)
            this.internalSetTeam(null)

            logger.info("Removed {} from {}", this, oldTeam)
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

    suspend fun logRound(
        points: Int = DEFAULT_ROUND_POINTS,
        manualLogged: Boolean = false
    ): LogRoundResult {
        val currentTime = Timestamps.now()

        event.mutex.withLock {
            val team = team.value
            if (!manualLogged) {
                val runTimer = event.runTimer

                if (!runTimer.runEnabled.value) {
                    logger.info("Tried to log round while run is disabled: {}", this)
                    return LogRoundResult.RunDisabled
                }

                if (runTimer.validateFinishedRunDryPhase(this))
                    return LogRoundResult.LastRoundAlreadyLogged

                if (!validateInTime(currentTime)) {
                    logger.info("Round count interval is too short: {}", this)
                    return LogRoundResult.ValidationFailed
                }
            }

            val newRound = Round(
                event = event,
                points = points,
                timestamp = currentTime,
                manuallyLogged = manualLogged,
                runner = this
            )

            this.internalAddRound(newRound)

            if (team != null) {
                newRound.internalSetTeam(team)
                team.internalAddRound(newRound)
            }

            logger.info("Logged {}", newRound)
            return LogRoundResult.Logged(newRound)
        }
    }

    private fun validateInTime(
        currentTime: LocalDateTime
    ): Boolean {
        val lastCountRunner = getLastCount(getRoundLogHistory()) ?: return true
        val timeDifference = currentTime.toMilliseconds() - lastCountRunner.toMilliseconds()
        return timeDifference > event.roundThreshold.value * 1000
    }

    private fun getRoundLogHistory(): List<Round> {
        val team = this.team.value ?: return this.rounds.value
        return team.rounds.value
    }

    private fun getLastCount(roundLog: List<Round>): LocalDateTime? {
        for (i in roundLog.indices.reversed()) {
            if (!roundLog[i].manuallyLogged.value) return roundLog[i].timestamp.value
        }
        return null
    }

    override suspend fun delete(): DeleteElementResult {
        event.mutex.withLock {
            if (rounds.value.isNotEmpty())
                return DeleteElementResult.NotDeleted("Runner has counted rounds")

            team.value?.internalRemoveRunner(this)
            event.internalRemoveRunner(this)

            return DeleteElementResult.Deleted
        }
    }

    override fun toString(): String {
        return "Runner(chipId=${chipId.value}, name=${name.value}, team=${team.value?.name ?: "-"})"
    }
}
