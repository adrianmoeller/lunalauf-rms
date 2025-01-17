package lunalauf.rms.model.serialization

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

    @OptIn(ExperimentalSerializationApi::class)
    override fun save(path: String, event: Event) {
        FileOutputStream(path).use {
            Json.encodeToStream(event.toSerializationModel(), it)
        }

    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun load(path: String): Event {
        FileInputStream(path).use {
            return Json.decodeFromStream<EventSM>(it).toInternalModel()
        }
    }
}