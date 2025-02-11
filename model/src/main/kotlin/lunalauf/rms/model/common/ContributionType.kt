package lunalauf.rms.model.common

import kotlinx.serialization.Serializable

@Serializable
enum class ContributionType {
    PER_ROUND, FIXED, BOTH, NONE
}