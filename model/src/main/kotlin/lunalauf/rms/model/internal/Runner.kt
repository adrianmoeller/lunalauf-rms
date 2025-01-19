package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import lunalauf.rms.model.common.ContributionType

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
    private val _chipId = MutableStateFlow(chipId)
    val chipId get() = _chipId.asStateFlow()

    private val _name = MutableStateFlow(name)
    val name get() = _name.asStateFlow()

    private val _team = MutableStateFlow<Team?>(null)
    val team get() = _team.asStateFlow()

    private val _rounds = MutableStateFlow(emptyList<Round>())
    val rounds get() = _rounds.asStateFlow()

    internal fun initSetTeam(team: Team?) {
        _team.update { team }
    }

    internal fun initSetRounds(rounds: List<Round>) {
        _rounds.update { rounds }
    }
}
