package lunalauf.rms.model.serialization

import kotlinx.serialization.Serializable

@Serializable
sealed class FunfactorSM {
    abstract val name: String
    abstract val description: String
    abstract val results: List<FunfactorResultSM>
}
