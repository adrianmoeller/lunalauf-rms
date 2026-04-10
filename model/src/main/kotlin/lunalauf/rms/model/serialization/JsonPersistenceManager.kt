package lunalauf.rms.model.serialization

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import lunalauf.rms.model.PersistenceManager
import lunalauf.rms.model.internal.Event
import lunalauf.rms.model.mapper.InternalModelMapper.Companion.toInternalModel
import lunalauf.rms.model.mapper.SerializationModelMapper.Companion.toSerializationModel
import java.io.FileInputStream
import java.io.FileOutputStream

class JsonPersistenceManager : PersistenceManager {

    private val scope = CoroutineScope(Dispatchers.IO)

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun save(path: String, event: Event, preSaveActions: () -> Unit) {
        val eventSM = event.mutex.withLock {
            event.toSerializationModel()
        }

        scope.launch {
            FileOutputStream(path).use {
                Json.encodeToStream(eventSM, it)
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun load(path: String): Event {
        val eventSM = withContext(scope.coroutineContext) {
            FileInputStream(path).use {
                Json.decodeFromStream<EventSM>(it)
            }
        }

        return eventSM.toInternalModel()
    }
}