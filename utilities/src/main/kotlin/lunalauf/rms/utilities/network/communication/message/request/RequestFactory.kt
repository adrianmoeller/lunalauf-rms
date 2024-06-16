package lunalauf.rms.utilities.network.communication.message.request

import java.util.*

class RequestFactory {
    private var messageIdCounter = Random().nextLong()

    private val nextMessageId: Long
        get() {
            messageIdCounter = (messageIdCounter + 1) % (Long.MAX_VALUE - 1)
            return messageIdCounter
        }

    fun createMinigameRecordRequest(runnerId: Long, minigameId: Int, points: Int): MinigameRecordRequest {
        val request = MinigameRecordRequest()
        request.messageId = nextMessageId
        request.runnerId = runnerId
        request.minigameId = minigameId
        request.points = points
        return request
    }

    fun createRoundCountRequest(runnerId: Long): RoundCountRequest {
        val request = RoundCountRequest()
        request.messageId = nextMessageId
        request.runnerId = runnerId
        return request
    }

    fun createRunnerInfoRequest(runnerId: Long): RunnerInfoRequest {
        val request = RunnerInfoRequest()
        request.messageId = nextMessageId
        request.runnerId = runnerId
        return request
    }
}
