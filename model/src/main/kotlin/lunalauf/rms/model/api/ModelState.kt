package lunalauf.rms.model.api

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import lunalauf.rms.model.internal.Event

sealed class ModelState {
    companion object {
        @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
        val modelContext = newSingleThreadContext("model")

        fun freeResources() {
            modelContext.close()
        }
    }

    data object Unloaded : ModelState()

    class Loaded(
        val path: String,
        val event: Event
    ) : ModelState()
}