package lunalauf.rms.model.internal

import lunalauf.rms.model.api.DeleteElementResult

sealed class EventChild(
    protected val event: Event
) {
    abstract suspend fun delete(): DeleteElementResult
}