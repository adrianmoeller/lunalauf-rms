package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
}