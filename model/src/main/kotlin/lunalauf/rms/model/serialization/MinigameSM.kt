package lunalauf.rms.model.serialization

import kotlinx.serialization.Serializable

@Serializable
data class MinigameSM(
    override val name: String,
    override val description: String,
    override val results: List<FunfactorResultSM>,
    val id: Int
) : FunfactorSM()
