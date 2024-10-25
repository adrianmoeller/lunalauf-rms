package lunalauf.rms.utilities.network.client

import lunalauf.rms.utilities.network.communication.ErrorType
import lunalauf.rms.utilities.network.communication.message.response.ErrorResponse
import lunalauf.rms.utilities.network.communication.message.response.Response
import lunalauf.rms.utilities.network.communication.message.response.RoundCountAcceptedResponse
import lunalauf.rms.utilities.network.communication.message.response.RoundCountRejectedResponse
import lunalauf.rms.utilities.network.util.FixedQueue

class RoundCounter(
    connection: Connection,
    onConnectionLost: () -> Unit
) : AbstractOperator(connection, onConnectionLost) {
    val successQueue: FixedQueue<RoundCountAcceptedResponse> = FixedQueue(4)

    override fun handleResponse(response: Response) {
        when (response) {
            is RoundCountRejectedResponse -> {
                mutableState.value = RespondedRejected(response)
            }

            is ErrorResponse -> {
                mutableState.value = State.Error(response.error)
            }

            is RoundCountAcceptedResponse -> {
                successQueue.push(response)
                mutableState.value = RespondedAccepted(response)
            }

            else -> {
                mutableState.value = State.Error(ErrorType.UNEXPECTED_SERVER_MESSAGE)
            }
        }
    }

    fun processInput(runnerId: Long) {
        if (repetitionHandler.isUnwantedRepetition(runnerId)) return
        requestSubmitter.submit(requestFactory.createRoundCountRequest(runnerId), connection)
    }

    class RespondedAccepted(val response: RoundCountAcceptedResponse) : State()
    class RespondedRejected(val response: RoundCountRejectedResponse) : State()
}
