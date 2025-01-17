package lunalauf.rms.model

import lunalauf.rms.model.internal.Event

interface PersistenceManager {

    fun initialize() {
        // NO-OP
    }

    fun save(path: String, event: Event)
    fun load(path: String): Event
}