package lunalauf.rms.utilities.network.client

import lunalauf.rms.utilities.network.communication.ErrorType
import lunalauf.rms.utilities.network.communication.message.response.ErrorResponse
import lunalauf.rms.utilities.network.communication.message.response.Response
import lunalauf.rms.utilities.network.communication.message.response.RoundCountAcceptedResponse
import lunalauf.rms.utilities.network.communication.message.response.RoundCountRejectedResponse
import lunalauf.rms.utilities.network.util.FixedQueue

class RoundCounter(
    val connection: Connection
) : CounterOperationMode() {
    private var onConnectionLost: () -> Unit = {}
    val successQueue: FixedQueue<RoundCountAcceptedResponse> = FixedQueue(4)

    override fun handleResponse(response: Response) {
        when (response) {
            is RoundCountRejectedResponse -> {
                _state.value = RespondedRejected(response)
            }

            is ErrorResponse -> {
                _state.value = State.Error(response.error)
            }

            is RoundCountAcceptedResponse -> {
                successQueue.push(response)
                _state.value = RespondedAccepted(response)
            }

            else -> {
                _state.value = State.Error(ErrorType.UNEXPECTED_SERVER_MESSAGE)
            }
        }
    }

    override fun handleError(error: ErrorType) {
        _state.value = State.Error(error)
        if (error == ErrorType.DISCONNECTED) onConnectionLost()
    }

    fun processInput(runnerId: Long) {
        if (repetitionHandler.isUnwantedRepetition(runnerId)) return
        requestSubmitter.submit(requestFactory.createRoundCountRequest(runnerId), connection)
    }

    fun setOnConnectionLost(onConnectionLost: () -> Unit) {
        this.onConnectionLost = onConnectionLost
    }

    class RespondedAccepted(val response: RoundCountAcceptedResponse) : State()
    class RespondedRejected(val response: RoundCountRejectedResponse) : State()
}
