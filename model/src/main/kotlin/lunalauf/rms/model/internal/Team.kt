package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import lunalauf.rms.model.common.ContributionType

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
    private val _name = MutableStateFlow(name)
    val name get() = _name.asStateFlow()

    private val _members = MutableStateFlow(emptyList<Runner>())
    val members get() = _members.asStateFlow()

    private val _rounds = MutableStateFlow(emptyList<Round>())
    val rounds get() = _rounds.asStateFlow()

    private val _funfactorResults = MutableStateFlow(emptyList<FunfactorResult>())
    val funfactorResults get() = _funfactorResults.asStateFlow()

    internal fun initSetMembers(members: List<Runner>) {
        this._members.update { members }
    }

    internal fun initSetRounds(rounds: List<Round>) {
        this._rounds.update { rounds }
    }

    internal fun initSetFunfactorResults(funfactorResults: List<FunfactorResult>) {
        this._funfactorResults.update { funfactorResults }
    }
}
