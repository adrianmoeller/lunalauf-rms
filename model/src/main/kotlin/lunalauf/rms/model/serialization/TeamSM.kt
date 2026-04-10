package lunalauf.rms.model.serialization

import kotlinx.serialization.Serializable
import lunalauf.rms.model.common.ContributionType

@Serializable
data class TeamSM(
    override val uid: Long,
    override val amountPerRound: Double,
    override val amountFix: Double,
    override val contributionType: ContributionType,
    val name: String,
    val members: List<RunnerSM>
) : ContributorSM()
