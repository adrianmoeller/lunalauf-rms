package lunalauf.rms.model.api

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.withLock
import lunalauf.rms.model.PersistenceManager
import lunalauf.rms.model.internal.Event
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(ModelManager.Companion::class.java)

sealed class ModelManager {
    companion object {
        const val FILE_EXTENSION = "llj"

        fun initialize(persistenceManager: PersistenceManager): ModelManager {
            return try {
                Available(persistenceManager)
            } catch (e: Exception) {
                InitializationError(
                    message = "Initializing persistence manager failed",
                    exception = e
                )
            }
        }
    }

    data class InitializationError(
        val message: String,
        val exception: Exception
    ) : ModelManager() {
        init {
            logger.error(message, exception)
        }
    }

    class Available(
        private val persistenceManager: PersistenceManager
    ) : ModelManager() {

        private var preSaveProcessing: () -> Unit = {}

        private var _model = MutableStateFlow<ModelState>(ModelState.Unloaded)
        val model = _model.asStateFlow()

        init {
            persistenceManager.initialize()
        }

        suspend fun new(path: String, year: Int): ModelResult {
            val constModel = _model.value

            _model.update { ModelState.Loading }

            if (constModel is ModelState.Loaded) {
                constModel.event.mutex.withLock {


                    try {
                        persistenceManager.save(path, constModel.event) { runPreSaveProcessing() }
                    } catch (e: Exception) {
                        _model.update { ModelState.Loaded(constModel.path, constModel.event) }
                        return ModelResult.Error("Failed saving model before creating new one", e)
                    }

                    return internalNew(path, year)
                }
            }

            return internalNew(path, year)
        }

        private suspend fun internalNew(path: String, year: Int): ModelResult {
            try {
                val newEvent = Event(year)
                persistenceManager.save(path, newEvent)
                _model.update { ModelState.Loaded(path, newEvent) }
            } catch (e: Exception) {
                _model.update { ModelState.Unloaded }
                return ModelResult.Error("Failed creating new file", e)
            }

            return ModelResult.Available
        }

        suspend fun load(path: String): ModelResult {
            val constModel = _model.value

            _model.update { ModelState.Loading }

            if (constModel is ModelState.Loaded) {
                constModel.event.mutex.withLock {
                    try {
                        persistenceManager.save(path, constModel.event) { runPreSaveProcessing() }
                    } catch (e: Exception) {
                        _model.update { ModelState.Loaded(constModel.path, constModel.event) }
                        return ModelResult.Error("Failed saving model before loading new one", e)
                    }

                    return internalLoad(path)
                }
            }

            return internalLoad(path)
        }

        private suspend fun internalLoad(path: String): ModelResult {
            try {
                val event = persistenceManager.load(path)
                _model.update { ModelState.Loaded(path, event) }
            } catch (e: Exception) {
                _model.update { ModelState.Unloaded }
                return ModelResult.Error("Failed loading file", e)
            }

            return ModelResult.Available
        }

        suspend fun save(): SaveResult {
            val constModel = _model.value
            if (constModel is ModelState.Loaded) {
                constModel.event.mutex.withLock {
                    try {
                        persistenceManager.save(constModel.path, constModel.event) { runPreSaveProcessing() }
                    } catch (e: Exception) {
                        return SaveResult.Error("Failed saving model", e)
                    }

                    return SaveResult.Success(constModel.path)
                }
            }

            return SaveResult.NoFileOpen
        }

        private fun runPreSaveProcessing() {
            try {
                preSaveProcessing()
            } catch (_: Exception) {
            }
        }

        suspend fun close(): CloseResult {
            val constModel = _model.value
            if (constModel is ModelState.Loaded) {
                constModel.event.mutex.withLock {
                    _model.update { ModelState.Unloaded }
                    return CloseResult.Success
                }
            }

            return CloseResult.NoFileOpen
        }

        fun removePreSaveProcessing() {
            preSaveProcessing = {}
        }

        fun setPreSaveProcessing(preSaveProcessing: () -> Unit) {
            this.preSaveProcessing = preSaveProcessing
        }
    }
}

sealed class ModelResult {
    data object Available : ModelResult()
    class Error(val message: String, exception: Exception? = null) : ModelResult() {
        init {
            if (exception == null) logger.error(message) else logger.error(message, exception)
        }
    }
}

sealed class SaveResult {
    data object NoFileOpen : SaveResult()
    class Success(val path: String) : SaveResult() {
        init {
            logger.info("Model saved")
        }
    }

    class Error(val message: String, exception: Exception) : SaveResult() {
        init {
            logger.error(message, exception)
        }
    }
}

sealed class CloseResult {
    data object NoFileOpen : CloseResult()
    data object Success : CloseResult()
}