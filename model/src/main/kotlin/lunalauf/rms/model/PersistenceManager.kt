package lunalauf.rms.model

import lunalauf.rms.model.internal.Event

interface PersistenceManager {

    fun initialize() {
        // NO-OP
    }

    suspend fun save(
        path: String,
        event: Event,
        preSaveActions: () -> Unit = {}
    )

    suspend fun load(path: String): Event
}