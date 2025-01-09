package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import lunalauf.rms.model.common.ContributionType

class Runner internal constructor(
    amountPerRound: Double,
    amountFix: Double,
    contributionType: ContributionType,
    chipId: Long,
    name: String,
    team: Team?,
    rounds: List<Round>
) : Contributor(
    amountPerRound,
    amountFix,
    contributionType
) {
    private val _chipId = MutableStateFlow(chipId)
    val chipId get() = _chipId.asStateFlow()

    private val _name = MutableStateFlow(name)
    val name get() = _name.asStateFlow()

    private val _team = MutableStateFlow(team)
    val team get() = _team.asStateFlow()

    private val _rounds = MutableStateFlow(rounds)
    val rounds get() = _rounds.asStateFlow()
}
