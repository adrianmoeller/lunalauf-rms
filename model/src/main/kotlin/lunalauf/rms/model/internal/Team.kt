package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import lunalauf.rms.model.common.ContributionType

class Team internal constructor(
    amountPerRound: Double,
    amountFix: Double,
    contributionType: ContributionType,
    name: String,
    members: List<Runner>,
    rounds: List<Round>,
    funfactorResults: List<FunfactorResult>
) : Contributor(
    amountPerRound,
    amountFix,
    contributionType
) {
    private val _name = MutableStateFlow(name)
    val name get() = _name.asStateFlow()

    private val _members = MutableStateFlow(members)
    val members get() = _members.asStateFlow()

    private val _rounds = MutableStateFlow(rounds)
    val rounds get() = _rounds.asStateFlow()

    private val _funfactorResults = MutableStateFlow(funfactorResults)
    val funfactorResults get() = _funfactorResults.asStateFlow()
}
