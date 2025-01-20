package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.withLock
import lunalauf.rms.model.api.UpdateMinigameIdResult
import org.slf4j.LoggerFactory

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
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val _id = MutableStateFlow(id)
    val id get() = _id.asStateFlow()

    suspend fun updateId(id: Int): UpdateMinigameIdResult {
        event.mutex.withLock {
            event.getMinigame(id)?.let {
                logger.warn("Missing UI check if minigame ID already exists when updating a minigame ID")
                return UpdateMinigameIdResult.Exists(it)
            }

            event.idToMinigame.remove(this.id.value)
            _id.update { id }
            event.idToMinigame[id] = this

            return UpdateMinigameIdResult.Updated
        }
    }
}
