package lunalauf.rms.model.common

import kotlinx.serialization.Serializable

@Serializable
enum class ChallengeState {
    PENDING, STARTED, COMPLETED
}