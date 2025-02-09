package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.withLock
import lunalauf.rms.model.api.DeleteElementResult
import lunalauf.rms.model.api.UpdateFunfactorNameResult
import org.slf4j.LoggerFactory

sealed class Funfactor(
    event: Event,
    name: String,
    description: String
) : EventChild(
    event
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val _name = MutableStateFlow(name)
    val name get() = _name.asStateFlow()

    private val _description = MutableStateFlow(description)
    val description get() = _description.asStateFlow()

    private val _results = MutableStateFlow(emptyList<FunfactorResult>())
    val results get() = _results.asStateFlow()

    internal fun internalSetResults(results: List<FunfactorResult>) {
        _results.update { results }
    }

    internal fun internalRemoveResult(result: FunfactorResult) {
        _results.update { it - result }
    }

    suspend fun updateName(name: String): UpdateFunfactorNameResult {
        event.mutex.withLock {
            if (name.isBlank()) {
                logger.warn("Missing UI check if name is not blank when updating a funfactor name")
                return UpdateFunfactorNameResult.BlankName
            }

            _name.update { name }

            return UpdateFunfactorNameResult.Updated
        }
    }

    suspend fun updateDescription(description: String) {
        event.mutex.withLock {
            _description.update { description }
        }
    }

    override suspend fun delete(): DeleteElementResult {
        event.mutex.withLock {
            if (results.value.isNotEmpty())
                return DeleteElementResult.NotDeleted("Funfactor has results")

            when (this) {
                is Minigame -> event.internalRemoveMinigame(this)
                is Challenge -> event.internalRemoveChallenge(this)
            }

            return DeleteElementResult.Deleted
        }
    }

    abstract suspend fun print(): String
}