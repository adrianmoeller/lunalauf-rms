package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.withLock
import lunalauf.rms.model.api.DeleteElementResult
import lunalauf.rms.model.common.ContributionType

sealed class Contributor(
    event: Event,
    name: String,
    amountPerRound: Double,
    amountFix: Double,
    contributionType: ContributionType
) : EventChild(
    event
) {
    private val _name = MutableStateFlow(name)
    val name get() = _name.asStateFlow()

    private val _amountPerRound = MutableStateFlow(amountPerRound)
    val amountPerRound get() = _amountPerRound.asStateFlow()

    private val _amountFix = MutableStateFlow(amountFix)
    val amountFix get() = _amountFix.asStateFlow()

    private val _contributionType = MutableStateFlow(contributionType)
    val contributionType get() = _contributionType.asStateFlow()

    private val _rounds = MutableStateFlow(emptyList<Round>())
    val rounds get() = _rounds.asStateFlow()

    val numOfRounds = rounds.map { it.sumOf { round -> round.points.value } }
        .stateIn(event.scope, SharingStarted.Eagerly, 0)

    internal fun internalSetRounds(rounds: List<Round>) {
        this._rounds.update { rounds }
    }

    internal fun internalAddRound(round: Round) {
        _rounds.update { it + round }
    }

    internal fun internalRemoveRound(round: Round) {
        _rounds.update { it - round }
    }

    suspend fun updateName(name: String) {
        event.mutex.withLock {
            _name.update { name }
        }
    }

    suspend fun updateContribution(
        type: ContributionType,
        amountFix: Double,
        amountPerRound: Double
    ) {
        event.mutex.withLock {
            _contributionType.update { type }
            _amountFix.update { amountFix }
            _amountPerRound.update { amountPerRound }
        }
    }

    abstract suspend fun delete(): DeleteElementResult
}