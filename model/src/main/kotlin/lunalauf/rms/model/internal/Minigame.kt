package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class Minigame internal constructor(
    name: String,
    description: String,
    results: List<FunfactorResult>,
    id: Int
) : Funfactor(
    name,
    description,
    results
) {
    private val _id = MutableStateFlow(id)
    val id get() = _id.asStateFlow()
}
