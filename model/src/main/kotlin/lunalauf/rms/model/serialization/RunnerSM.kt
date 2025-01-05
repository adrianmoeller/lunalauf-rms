package lunalauf.rms.model.serialization

import kotlinx.serialization.Serializable
import lunalauf.rms.model.common.ContributionType

@Serializable
data class RunnerSM(
    override val uid: Long,
    override val amountPerRound: Double,
    override val amountFix: Double,
    override val contributionType: ContributionType,
    val chipId: Long,
    val name: String,
    val rounds: List<RoundSM>
) : ContributorSM()
