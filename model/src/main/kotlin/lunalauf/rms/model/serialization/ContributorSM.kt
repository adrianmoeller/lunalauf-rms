package lunalauf.rms.model.serialization

import kotlinx.serialization.Serializable
import lunalauf.rms.model.common.ContributionType

@Serializable
sealed class ContributorSM {
    abstract val uid: Long
    abstract val amountPerRound: Double
    abstract val amountFix: Double
    abstract val contributionType: ContributionType
}
