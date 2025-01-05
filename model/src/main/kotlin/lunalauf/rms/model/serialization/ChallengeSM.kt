package lunalauf.rms.model.serialization

import kotlinx.serialization.Serializable
import lunalauf.rms.model.common.ChallengeState

@Serializable
data class ChallengeSM(
    override val name: String,
    override val description: String,
    override val results: List<FunfactorResultSM>,
    val expires: Boolean,
    val expireMsg: String,
    val duration: Int,
    val state: ChallengeState,
    val receiveImages: Boolean
) : FunfactorSM()
