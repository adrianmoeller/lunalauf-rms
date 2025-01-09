package lunalauf.rms.model.serialization

import kotlinx.serialization.Serializable

@Serializable
data class EventSM(
    val year: Int,
    val runDuration: Int,
    val sponsorPoolAmount: Double,
    val sponsorPoolRounds: Int,
    val additionalContribution: Double,
    val teams: List<TeamSM>,
    val singleRunners: List<RunnerSM>,
    val minigames: List<MinigameSM>,
    val challenges: List<ChallengeSM>,
    val connections: List<ConnectionEntrySM>
)
