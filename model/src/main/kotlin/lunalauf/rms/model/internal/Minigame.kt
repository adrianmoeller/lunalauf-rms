package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class Minigame internal constructor(
    event: Event,
    name: String,
    description: String,
    id: Int
) : Funfactor(
    event,
    name,
    description
) {
    private val _id = MutableStateFlow(id)
    val id get() = _id.asStateFlow()
}
