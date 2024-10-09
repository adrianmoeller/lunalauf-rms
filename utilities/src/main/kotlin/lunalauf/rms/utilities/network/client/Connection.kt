package lunalauf.rms.utilities.network.client

import kotlinx.coroutines.flow.StateFlow
import lunalauf.rms.utilities.network.communication.message.Message
import java.io.IOException

interface Connection {
    val status: StateFlow<Int>
    val ping: StateFlow<Long>

    @Throws(NoConnectionException::class)
    fun sendMessage(message: Message)
    @Throws(
        NoAnswerException::class,
        NoConnectionException::class,
        CorruptedMessageException::class
    )
    fun receiveMessage(): Message
    @Throws(IOException::class)
    fun close()

    class NoConnectionException : Exception {
        constructor() : super()
        constructor(cause: Throwable?) : super(cause)
    }

    class NoAnswerException : Exception {
        constructor() : super()
        constructor(cause: Throwable?) : super(cause)
    }

    class CorruptedMessageException : Exception {
        constructor() : super()
        constructor(cause: Throwable?) : super(cause)
    }
}