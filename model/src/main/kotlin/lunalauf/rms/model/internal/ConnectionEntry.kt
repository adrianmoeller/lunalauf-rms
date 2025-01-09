package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConnectionEntry internal constructor(
    chatId: Long,
    runner: Runner
) {
    private val _chatId = MutableStateFlow(chatId)
    val chatId get() = _chatId.asStateFlow()

    private val _runner = MutableStateFlow(runner)
    val runner get() = _runner.asStateFlow()
}
