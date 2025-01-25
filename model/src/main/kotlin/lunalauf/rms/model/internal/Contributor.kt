package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.withLock
import lunalauf.rms.model.api.DeleteElementResult
import lunalauf.rms.model.common.ContributionType

sealed class Contributor(
    event: Event,
    amountPerRound: Double,
    amountFix: Double,
    contributionType: ContributionType
) : EventChild(
    event
) {
    private val _amountPerRound = MutableStateFlow(amountPerRound)
    val amountPerRound get() = _amountPerRound.asStateFlow()

    private val _amountFix = MutableStateFlow(amountFix)
    val amountFix get() = _amountFix.asStateFlow()

    private val _contributionType = MutableStateFlow(contributionType)
    val contributionType get() = _contributionType.asStateFlow()

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