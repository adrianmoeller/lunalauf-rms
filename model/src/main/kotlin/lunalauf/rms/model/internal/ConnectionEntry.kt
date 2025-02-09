package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import lunalauf.rms.model.api.DeleteElementResult

class ConnectionEntry internal constructor(
    event: Event,
    chatId: Long,
    runner: Runner
) : EventChild(
    event
) {
    private val _chatId = MutableStateFlow(chatId)
    val chatId get() = _chatId.asStateFlow()

    private val _runner = MutableStateFlow(runner)
    val runner get() = _runner.asStateFlow()

    override suspend fun delete(): DeleteElementResult {
        throw UnsupportedOperationException()
    }
}
